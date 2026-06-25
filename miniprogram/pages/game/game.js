const api = require("../../utils/api.js");
const timeline = require("../../utils/timeline.js");
const dynasties = require("../../utils/dynasties.js");

function formatModernCityLabel(name) {
  if (!name) return "";
  var s = String(name).trim();
  if (!s) return "";
  if (s.indexOf("(今") === 0) return s;
  return "(今" + s + ")";
}

function formatAnswerLegendLabel(historicalCity, modernPlace) {
  var city = (historicalCity || "").trim();
  var modern = formatModernCityLabel(modernPlace || "");
  if (city && modern) return city + modern;
  if (city) return city;
  if (modern) return modern;
  return "";
}

function isValidCoord(lat, lng) {
  return (
    !isNaN(lat) &&
    !isNaN(lng) &&
    Math.abs(lat) <= 90 &&
    Math.abs(lng) <= 180
  );
}

function computeResultMapScale(gLat, gLng, aLat, aLng, distanceKm) {
  if (
    !isValidCoord(gLat, gLng) ||
    !isValidCoord(aLat, aLng)
  ) {
    return 12;
  }

  var latMid = (gLat + aLat) / 2;
  var latRad = (latMid * Math.PI) / 180;
  var latSpan = Math.abs(gLat - aLat);
  var lngSpan = Math.abs(gLng - aLng) * Math.cos(latRad);
  var spanDeg = Math.max(latSpan, lngSpan, 0.0001);

  // 留边距，保证 marker + 连线都在可视范围内
  var paddedKm = Math.max(
    (Number(distanceKm) || 0) * 1.6,
    spanDeg * 111 * 1.85
  );

  // scale 越大越近；距离远则缩小 scale
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

function buildResultMapState(result) {
  var gLat = Number(result.guessLatitude);
  var gLng = Number(result.guessLongitude);
  var aLat = Number(result.answerLatitude);
  var aLng = Number(result.answerLongitude);
  var distance = Number(result.distanceKm) || 0;
  var hasGuess = isValidCoord(gLat, gLng);
  var hasAnswer =
    result.answerLatitude != null &&
    result.answerLongitude != null &&
    isValidCoord(aLat, aLng);
  var hasLine = hasGuess && hasAnswer;
  var polyline = [];
  var markers = [];

  if (hasGuess) {
    markers.push({
      id: 1,
      latitude: gLat,
      longitude: gLng,
      title: "你",
      width: 36,
      height: 36,
      label: {
        content: "你",
        color: "#ffffff",
        fontSize: 11,
        bgColor: "#1a1f2e",
        borderRadius: 6,
        padding: 4,
        anchorX: 0,
        anchorY: -36,
      },
    });
  }

  if (hasAnswer) {
    markers.push({
      id: 2,
      latitude: aLat,
      longitude: aLng,
      title: "答",
      width: 36,
      height: 36,
      label: {
        content: "答",
        color: "#1a1f2e",
        fontSize: 11,
        bgColor: "#f5d78e",
        borderRadius: 6,
        padding: 4,
        anchorX: 0,
        anchorY: -36,
      },
    });
  }

  if (hasLine) {
    polyline = [
      {
        points: [
          { latitude: gLat, longitude: gLng },
          { latitude: aLat, longitude: aLng },
        ],
        color: "#1a1a1aFF",
        width: 8,
        dottedLine: true,
        arrowLine: false,
      },
    ];
  }

  var distanceLabel =
    distance > 0.1 ? Math.round(distance * 10) / 10 + " km" : "";

  return {
    resultMapLat: hasLine
      ? (gLat + aLat) / 2
      : hasGuess
        ? gLat
        : hasAnswer
          ? aLat
          : 35,
    resultMapLng: hasLine
      ? (gLng + aLng) / 2
      : hasGuess
        ? gLng
        : hasAnswer
          ? aLng
          : 105,
    resultMapScale: computeResultMapScale(
      gLat,
      gLng,
      aLat,
      aLng,
      distance
    ),
    resultMarkers: markers,
    resultPolyline: polyline,
    resultDistanceLabel: distanceLabel,
  };
}

Page({
  data: {
    roundIndex: 0,
    round: {},
    mapLat: 39.9,
    mapLng: 116.4,
    pickMapScale: 9,
    pickLat: 0,
    pickLng: 0,
    markers: [],
    picked: false,
    pickedCity: "",
    cityResolving: false,
    yearAd: 0,
    displayYearShort: "",
    currentDynasty: "",
    currentDynastyTitle: "",
    scrollLeft: 0,
    rulerSegments: [],
    rulerTicks: [],
    rulerInnerWidth: 0,
    rulerTotalWidth: 0,
    sidePad: 0,
    mapExpanded: false,
    submitting: false,
    _scrollLock: false,
    resultVisible: false,
    resultSheetShow: false,
    resultTotalScore: 0,
    resultDynastyScore: 0,
    resultGeoScore: 0,
    resultDistanceKm: 0,
    resultDistanceLabel: "",
    resultGuessDynastyTitle: "",
    resultAnswerDynastyTitle: "",
    resultGuessCity: "",
    resultAnswerCity: "",
    resultAnswerCityLabel: "",
    resultAnswerLegendText: "",
    resultDynastyOk: false,
    resultAnswerLocation: "",
    resultAnswerTime: "",
    resultNextLabel: "下一题",
    resultMapLat: 35,
    resultMapLng: 105,
    resultMapScale: 12,
    resultMapKey: "",
    resultMarkers: [],
    resultPolyline: [],
    resultKnowledgeTitle: "",
    resultAnecdoteTitle: "",
    resultKnowledgeText: "",
    resultKnowledgeImage: "",
    resultBaikeUrl: "",
    resultEventTime: "",
  },

  onLoad: function (options) {
    var index = Number(options.index || 0);
    var app = getApp();
    var round = app.globalData.rounds[index];

    if (!round) {
      tt.showToast({ title: "题目不存在", icon: "none" });
      tt.navigateBack();
      return;
    }

    var sys = tt.getSystemInfoSync();
    var winW = sys.windowWidth;
    var innerWidth = timeline.contentWidthPx();
    var defaultYear = timeline.defaultYear();

    this.setData({
      roundIndex: index,
      round: round,
      yearAd: defaultYear,
      displayYearShort: timeline.formatYearShort(defaultYear),
      currentDynasty: "",
      currentDynastyTitle: "",
      rulerSegments: timeline.buildSegments(),
      rulerTicks: timeline.buildTicks(),
      rulerInnerWidth: innerWidth,
      rulerTotalWidth: innerWidth + winW,
      sidePad: winW / 2,
      scrollLeft: timeline.yearToScrollLeft(defaultYear),
    });
  },

  previewImage: function () {
    if (!this.data.round.imageUrl || this.data.resultVisible) return;
    tt.previewImage({ urls: [this.data.round.imageUrl] });
  },

  expandMap: function () {
    if (this.data.resultVisible) return;
    if (!this._mapViewReady) {
      this._mapViewReady = true;
    }
    this.setData({
      mapExpanded: true,
    });
  },

  collapseMap: function () {
    this.setData({ mapExpanded: false });
  },

  noop: function () {},

  onMapTap: function (e) {
    if (!this.data.mapExpanded || this.data.resultVisible) return;

    var lat = e.detail.latitude;
    var lng = e.detail.longitude;
    var self = this;

    this.setData({
      pickLat: lat,
      pickLng: lng,
      picked: false,
      pickedCity: "",
      cityResolving: true,
      markers: [
        {
          id: 1,
          latitude: lat,
          longitude: lng,
          width: 32,
          height: 32,
          callout: {
            content: "识别中…",
            display: "ALWAYS",
            padding: 6,
            borderRadius: 8,
            fontSize: 12,
            bgColor: "rgba(255,255,255,0.9)",
            color: "#b8860b",
          },
        },
      ],
    });

    api
      .reverseCity(lat, lng)
      .then(function (data) {
        var city = data.city || "未知地区";
        self.setData({
          picked: true,
          pickedCity: city,
          cityResolving: false,
          markers: [
            {
              id: 1,
              latitude: lat,
              longitude: lng,
              width: 32,
              height: 32,
            },
          ],
        });
      })
      .catch(function (err) {
        tt.showToast({
          title: (err && err.message) || "城市识别失败，请检查网络",
          icon: "none",
        });
        self.setData({
          picked: false,
          pickedCity: "",
          cityResolving: false,
          markers: [],
        });
      });
  },

  onRulerScroll: function (e) {
    if (this.data._scrollLock || this.data.resultVisible) return;
    var year = timeline.offsetToYear(e.detail.scrollLeft);
    if (year === this.data.yearAd) return;
    var dynasty = timeline.dynastyAt(year);
    this.setData({
      yearAd: year,
      displayYearShort: timeline.formatYearShort(year),
      currentDynasty: dynasty,
      currentDynastyTitle: dynasty ? timeline.dynastyTitleAt(year) : "",
    });
  },

  onRulerTouchEnd: function () {
    if (this.data.resultVisible) return;
    var self = this;
    var target = timeline.yearToScrollLeft(this.data.yearAd);
    this.setData({ _scrollLock: true });
    this.setData({ scrollLeft: target + 0.01 });
    setTimeout(function () {
      self.setData({ scrollLeft: target, _scrollLock: false });
    }, 50);
  },

  showResultPanel: function (result) {
    var self = this;
    var app = getApp();
    var isLast = this.data.roundIndex >= app.globalData.rounds.length - 1;
    var mapState = buildResultMapState(result);
    var answer = result.answer || {};

    this.setData(
      Object.assign(
        {
          resultVisible: true,
          resultSheetShow: false,
          mapExpanded: false,
          resultTotalScore: result.totalScore,
          resultDynastyScore: result.dynastyScore,
          resultGeoScore: result.geoScore,
          resultDistanceKm: result.distanceKm,
          resultGuessDynastyTitle: dynasties.titleOf(result.guessDynasty),
          resultAnswerDynastyTitle: dynasties.titleOf(result.answerDynasty),
          resultGuessCity: result.guessCity || "",
          resultAnswerCity: result.answerCity || "",
          resultAnswerCityLabel: formatModernCityLabel(
            (answer.modernPlace || result.answerCity || "").trim()
          ),
          resultAnswerLegendText: formatAnswerLegendLabel(
            answer.historicalCityName ||
              (answer.modernPlace || "").replace(/市$/, ""),
            answer.modernPlace
          ),
          resultDynastyOk: result.dynastyScore === 100,
          resultAnswerLocation: answer.locationName || "",
          resultEventTime: answer.timeLabel || "",
          resultKnowledgeTitle: answer.locationName || "",
          resultAnecdoteTitle: answer.anecdoteTitle || answer.sceneType || "",
          resultKnowledgeText: answer.knowledgeSummary || "",
          resultKnowledgeImage: this.data.round.imageUrl || "",
          resultBaikeUrl: answer.baikeUrl || "",
          resultNextLabel: isLast ? "查看总成绩" : "下一题",
          resultPolyline: mapState.resultPolyline,
          resultMapLat: mapState.resultMapLat,
          resultMapLng: mapState.resultMapLng,
          resultMapScale: mapState.resultMapScale,
          resultMarkers: mapState.resultMarkers,
          resultDistanceLabel: mapState.resultDistanceLabel,
        },
        {}
      )
    );

    setTimeout(function () {
      self.setData({ resultSheetShow: true });
    }, 300);
  },

  onResultSheetPreviewImage: function (e) {
    var url = e.detail.url;
    if (url) {
      tt.previewImage({ urls: [url] });
    }
  },

  onResultSheetOpenBaike: function () {
    var url = this.data.resultBaikeUrl;
    if (!url) return;
    tt.setClipboardData({
      data: url,
      success: function () {
        tt.showToast({ title: "百科链接已复制", icon: "none" });
      },
    });
  },

  onSubmit: function () {
    var self = this;
    var round = this.data.round;

    api
      .submitGuess({
        roundId: round.id,
        yearAd: this.data.yearAd,
        latitude: this.data.pickLat,
        longitude: this.data.pickLng,
        token: getApp().getToken(),
      })
      .then(function (result) {
        var app = getApp();
        app.globalData.sessionTotalScore += result.totalScore;
        app.globalData.sessionRoundScores.push(result.totalScore);
        app.globalData.lastGuessResult = result;
        self.showResultPanel(result);
      })
      .catch(function (err) {
        tt.showToast({ title: err.message || "提交失败", icon: "none" });
      })
      .finally(function () {
        self.setData({ submitting: false });
      });
  },

  onResultNext: function () {
    var app = getApp();
    var nextIndex = this.data.roundIndex + 1;

    if (nextIndex >= app.globalData.rounds.length) {
      tt.showModal({
        title: "挑战完成",
        content: "本局总分 " + app.globalData.sessionTotalScore + " 分",
        showCancel: false,
        success: function () {
          tt.reLaunch({ url: "/pages/index/index" });
        },
      });
      return;
    }

    tt.redirectTo({ url: "/pages/game/game?index=" + nextIndex });
  },
});
