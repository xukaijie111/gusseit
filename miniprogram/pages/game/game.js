const api = require("../../utils/api.js");
const timeline = require("../../utils/timeline.js");
const dynasties = require("../../utils/dynasties.js");

function computeResultMapScale(distanceKm) {
  // 城市级视野：最近约 scale 6（~50km），避免缩放到街道/村级
  var viewKm = Math.max(55, (distanceKm || 0) * 1.35);
  if (viewKm <= 70) return 6;
  if (viewKm <= 180) return 5;
  if (viewKm <= 450) return 4;
  if (viewKm <= 1000) return 3;
  return 2;
}

function buildResultMapState(result) {
  var gLat = Number(result.guessLatitude);
  var gLng = Number(result.guessLongitude);
  var aLat = Number(result.answerLatitude);
  var aLng = Number(result.answerLongitude);
  var distance = Number(result.distanceKm) || 0;
  var hasAnswer =
    !isNaN(aLat) && !isNaN(aLng) && result.answerLatitude != null;
  var polyline = [];
  var markers = [
    {
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
    },
  ];

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
    polyline = [
      {
        points: [
          { latitude: gLat, longitude: gLng },
          { latitude: aLat, longitude: aLng },
        ],
        color: "#1a1a1a",
        width: 4,
        dottedLine: true,
        arrowLine: false,
      },
    ];
  }

  return {
    resultMapLat: hasAnswer ? (gLat + aLat) / 2 : gLat,
    resultMapLng: hasAnswer ? (gLng + aLng) / 2 : gLng,
    resultMapScale: computeResultMapScale(distance),
    resultMarkers: markers,
    resultPolyline: polyline,
    resultDistanceLabel: distance > 0.1 ? distance + " km" : "同城",
    resultMapKey: "map-" + Date.now(),
  };
}

Page({
  data: {
    roundIndex: 0,
    round: {},
    mapLat: 35.0,
    mapLng: 105.0,
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
    resultDynastyOk: false,
    resultAnswerLocation: "",
    resultAnswerTime: "",
    resultNextLabel: "下一题",
    resultMapLat: 35,
    resultMapLng: 105,
    resultMapScale: 5,
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
    var patch = { mapExpanded: true };
    if (!this._mapViewReady) {
      patch.mapLat = 35.0;
      patch.mapLng = 105.0;
      this._mapViewReady = true;
    }
    this.setData(patch);
  },

  collapseMap: function () {
    var patch = { mapExpanded: false };
    if (this.data.picked && this.data.pickLat && this.data.pickLng) {
      patch.mapLat = this.data.pickLat;
      patch.mapLng = this.data.pickLng;
    }
    this.setData(patch);
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
              callout: {
                content: city,
                display: "ALWAYS",
                padding: 8,
                borderRadius: 8,
                fontSize: 13,
                bgColor: "rgba(255,255,255,0.9)",
                color: "#b8860b",
              },
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
          resultDynastyOk: result.dynastyScore === 100,
          resultAnswerLocation: answer.locationName || "",
          resultEventTime: answer.timeLabel || "",
          resultKnowledgeTitle: answer.locationName || "",
          resultAnecdoteTitle: answer.anecdoteTitle || answer.sceneType || "",
          resultKnowledgeText: answer.knowledgeSummary || "",
          resultKnowledgeImage: this.data.round.imageUrl || "",
          resultBaikeUrl: answer.baikeUrl || "",
          resultNextLabel: isLast ? "查看总成绩" : "下一题",
          resultPolyline: [],
          resultMapLat: mapState.resultMapLat,
          resultMapLng: mapState.resultMapLng,
          resultMapScale: mapState.resultMapScale,
          resultMarkers: mapState.resultMarkers,
          resultDistanceLabel: mapState.resultDistanceLabel,
          resultMapKey: mapState.resultMapKey,
        },
        {}
      )
    );

    setTimeout(function () {
      self._pendingResultPolyline = mapState.resultPolyline;
      self.setData({
        resultPolyline: mapState.resultPolyline,
        resultSheetShow: true,
      });
    }, 350);
  },

  onResultMapUpdated: function () {
    var pending = this._pendingResultPolyline;
    if (!pending || !pending.length) return;
    if (this.data.resultPolyline && this.data.resultPolyline.length) {
      this._pendingResultPolyline = null;
      return;
    }
    this.setData({ resultPolyline: pending });
    this._pendingResultPolyline = null;
  },

  onOpenBaike: function () {
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
    if (
      this.data.submitting ||
      !this.data.picked ||
      this.data.cityResolving ||
      this.data.resultVisible
    ) {
      return;
    }

    this.setData({ submitting: true, mapExpanded: false });
    var self = this;
    var round = this.data.round;

    api
      .submitGuess({
        roundId: round.id,
        yearAd: this.data.yearAd,
        latitude: this.data.pickLat,
        longitude: this.data.pickLng,
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
