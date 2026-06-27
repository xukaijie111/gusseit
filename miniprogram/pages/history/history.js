const api = require("../../utils/api.js");
const dynasties = require("../../utils/dynasties.js");

const PAGE_SIZE = 20;

function isValidCoord(lat, lng) {
  return !isNaN(lat) && !isNaN(lng) && Math.abs(lat) <= 90 && Math.abs(lng) <= 180;
}

function computeResultMapScale(gLat, gLng, aLat, aLng, distanceKm) {
  if (!isValidCoord(gLat, gLng) || !isValidCoord(aLat, aLng)) return 12;
  var latMid = (gLat + aLat) / 2;
  var latRad = (latMid * Math.PI) / 180;
  var latSpan = Math.abs(gLat - aLat);
  var lngSpan = Math.abs(gLng - aLng) * Math.cos(latRad);
  var spanDeg = Math.max(latSpan, lngSpan, 0.0001);
  var paddedKm = Math.max((Number(distanceKm) || 0) * 1.6, spanDeg * 111 * 1.85);
  if (paddedKm <= 3) return 14;
  if (paddedKm <= 8) return 13;
  if (paddedKm <= 20) return 12;
  if (paddedKm <= 45) return 11;
  if (paddedKm <= 90) return 10;
  if (paddedKm <= 180) return 9;
  if (paddedKm <= 350) return 8;
  if (paddedKm <= 700) return 7;
  if (paddedKm <= 1400) return 6;
  return 5;
}

function buildMapState(row) {
  var gLat = Number(row.guessLat);
  var gLng = Number(row.guessLng);
  var aLat = Number(row.answerLat);
  var aLng = Number(row.answerLng);
  var distance = Number(row.distanceKm) || 0;
  var hasGuess = isValidCoord(gLat, gLng);
  var hasAnswer = isValidCoord(aLat, aLng);
  var hasLine = hasGuess && hasAnswer;

  var markers = [];
  var polyline = [];

  if (hasGuess) {
    markers.push({
      id: 1, latitude: gLat, longitude: gLng, width: 36, height: 36,
      label: { content: "你", color: "#ffffff", fontSize: 11, bgColor: "#1a1f2e", borderRadius: 6, padding: 4, anchorX: 0, anchorY: -36 },
    });
  }
  if (hasAnswer) {
    markers.push({
      id: 2, latitude: aLat, longitude: aLng, width: 36, height: 36,
      label: { content: "答", color: "#1a1f2e", fontSize: 11, bgColor: "#f5d78e", borderRadius: 6, padding: 4, anchorX: 0, anchorY: -36 },
    });
  }

  if (hasLine) {
    polyline.push({
      points: [{ latitude: gLat, longitude: gLng }, { latitude: aLat, longitude: aLng }],
      color: "#b8860b", width: 3, dottedLine: false, arrowLine: true,
    });
  }

  var midLat = hasAnswer ? aLat : (hasGuess ? gLat : 35);
  var midLng = hasAnswer ? aLng : (hasGuess ? gLng : 105);
  if (hasLine) {
    midLat = (gLat + aLat) / 2;
    midLng = (gLng + aLng) / 2;
  }

  return {
    mapLat: midLat,
    mapLng: midLng,
    mapScale: computeResultMapScale(gLat, gLng, aLat, aLng, distance),
    markers: markers,
    polyline: polyline,
  };
}

Page({
  data: {
    sections: [],
    loading: true,
    moreLoading: false,
    hasMore: true,
    offset: 0,
    sheetVisible: false,
    sheetData: null,
    sheetMapLat: 35,
    sheetMapLng: 105,
    sheetMapScale: 12,
    sheetMarkers: [],
    sheetPolyline: [],
    sheetDistanceLabel: "",
    sheetAnswerLabel: "",
    sheetKnowledge: "",
    sheetAnecdoteTitle: "",
    sheetLocation: "",
  },

  onLoad: function () {
    this.loadMore();
  },

  onScrollToLower: function () {
    if (this.data.moreLoading || !this.data.hasMore) return;
    this.loadMore();
  },

  loadMore: function () {
    var self = this;
    var isFirst = this.data.offset === 0;
    this.setData(isFirst ? { loading: true } : { moreLoading: true });

    api
      .fetchHistory(this.data.offset, PAGE_SIZE)
      .then(function (data) {
        var newRows = (data.rows || []).map(function (r) {
          r.guessDynastyTitle = dynasties.titleOf(r.guessDynasty);
          r.answerDynastyTitle = dynasties.titleOf(r.answerDynasty);
          return r;
        });

        var merged = isFirst ? newRows : self.data.sections.flatMap(function (s) { return s.rows; }).concat(newRows);
        var sections = self._groupByDate(merged);

        self.setData({
          sections: sections,
          loading: false,
          moreLoading: false,
          hasMore: data.hasMore,
          offset: self.data.offset + newRows.length,
        });
      })
      .catch(function (err) {
        self.setData({ loading: false, moreLoading: false });
        tt.showToast({ title: err.message || "加载失败", icon: "none" });
      });
  },

  _groupByDate: function (rows) {
    var sections = [];
    var currentLabel = "";
    var currentRows = [];
    var now = new Date();
    var today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    var yesterday = new Date(today.getTime() - 86400000);
    var dayBefore = new Date(today.getTime() - 172800000);

    rows.forEach(function (r) {
      var d = new Date(r.answeredAt);
      var day = new Date(d.getFullYear(), d.getMonth(), d.getDate());
      var label;
      if (day.getTime() === today.getTime()) {
        label = "今天";
      } else if (day.getTime() === yesterday.getTime()) {
        label = "昨天";
      } else if (day.getTime() === dayBefore.getTime()) {
        label = "前天";
      } else {
        label = (d.getMonth() + 1) + "月" + d.getDate() + "日";
      }

      if (label !== currentLabel) {
        if (currentRows.length > 0) {
          sections.push({ label: currentLabel, rows: currentRows.slice() });
        }
        currentLabel = label;
        currentRows = [r];
      } else {
        currentRows.push(r);
      }
    });

    if (currentRows.length > 0) {
      sections.push({ label: currentLabel, rows: currentRows.slice() });
    }
    return sections;
  },

  onTapCard: function (e) {
    var sectionIdx = Number(e.currentTarget.dataset.section);
    var index = Number(e.currentTarget.dataset.index);
    var section = this.data.sections[sectionIdx];
    if (!section || !section.rows) return;
    var row = section.rows[index];
    if (!row) return;

    var dist = Number(row.distanceKm) || 0;
    var distLabel = dist > 0.1 ? Math.round(dist * 10) / 10 + " km" : "同城";

    var historicalCity = row.historicalCity || "";
    var modernPlace = row.modernPlace || "";
    var answerCity = row.answerCity || "";
    var answerLabel = historicalCity
      ? historicalCity + "(今" + (modernPlace || answerCity) + ")"
      : answerCity;

    var knowledge = row.knowledgeSummary || "";
    var anecdoteTitle = row.anecdoteTitle || "";
    var location = row.locationName || "";

    var mapState = buildMapState(row);

    this.setData({
      sheetData: row,
      sheetVisible: false,
      sheetMapLat: mapState.mapLat,
      sheetMapLng: mapState.mapLng,
      sheetMapScale: mapState.mapScale,
      sheetMarkers: mapState.markers,
      sheetPolyline: mapState.polyline,
      sheetDistanceLabel: distLabel,
      sheetAnswerLabel: answerLabel,
      sheetKnowledge: knowledge,
      sheetAnecdoteTitle: anecdoteTitle,
      sheetLocation: location,
    });

    var self = this;
    setTimeout(function () {
      self.setData({ sheetVisible: true });
    }, 100);
  },

  onCloseSheet: function () {
    var self = this;
    this.setData({ sheetVisible: false });
    setTimeout(function () {
      self.setData({ sheetData: null });
    }, 400);
  },

  onPreviewSheetImage: function (e) {
    var url = (e.detail && e.detail.url) || e.currentTarget.dataset.url;
    if (url) {
      tt.previewImage({ urls: [url] });
    }
  },
});
