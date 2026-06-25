const config = require("../../config.js");
const dynasties = require("../../utils/dynasties.js");
const api = require("../../utils/api.js");

var SNAP_WIDTH = 96;

function buildEraSegments(options) {
  var segments = [];
  var i;
  for (i = 0; i < options.length; i++) {
    segments.push({
      name: options[i].ruler,
      left: i * SNAP_WIDTH,
      width: SNAP_WIDTH,
    });
  }
  return segments;
}

function buildDecorTicks(segmentCount) {
  var ticks = [];
  var i;
  for (i = 0; i <= segmentCount; i++) {
    ticks.push({
      year: i,
      left: i * SNAP_WIDTH,
      size: i % 2 === 0 ? "mid" : "short",
      label: "",
    });
  }
  return ticks;
}

Page({
  data: {
    homeVideoSrc: config.homeVideoSrc,
    eraOptions: [],
    eraIndex: -1,
    selectedEraTitle: "",
    selectedEraSubtitle: "",
    countOptions: ["3", "5", "8"],
    countIndex: 1,
    loading: false,
    scrollLeft: 0,
    eraSegments: [],
    rulerTicks: [],
    rulerInnerWidth: SNAP_WIDTH,
    rulerTotalWidth: 0,
    sidePad: 0,
    _scrollLock: false,
  },

  onLoad: function () {
    var sys = tt.getSystemInfoSync();
    var winW = sys.windowWidth;
    var options = dynasties.eraPickerOptions();
    var innerWidth = options.length * SNAP_WIDTH;

    this.setData({
      eraOptions: options,
      eraSegments: buildEraSegments(options),
      rulerTicks: buildDecorTicks(options.length),
      rulerInnerWidth: innerWidth,
      rulerTotalWidth: innerWidth + winW,
      sidePad: winW / 2,
      scrollLeft: Math.max(0, (innerWidth - SNAP_WIDTH) / 2),
    });
  },

  snapToEra: function (index, animate) {
    var scrollLeft = index * SNAP_WIDTH;
    if (animate === false) {
      this.setData({ scrollLeft: scrollLeft, eraIndex: index });
      this.updateEraLabel(index);
      return;
    }
    var self = this;
    this.setData({ _scrollLock: true });
    this.setData({ scrollLeft: scrollLeft + 0.01 });
    setTimeout(function () {
      self.setData({
        scrollLeft: scrollLeft,
        eraIndex: index,
        _scrollLock: false,
      });
      self.updateEraLabel(index);
    }, 50);
  },

  updateEraLabel: function (index) {
    var item = this.data.eraOptions[index] || dynasties.ERA_ALL;
    this.setData({
      selectedEraTitle: item.title,
      selectedEraSubtitle: item.subtitle,
    });
  },

  onRulerScroll: function (e) {
    if (this.data._scrollLock) return;
    var index = Math.round(e.detail.scrollLeft / SNAP_WIDTH);
    index = Math.min(Math.max(index, 0), this.data.eraOptions.length - 1);
    if (index === this.data.eraIndex) return;
    this.setData({ eraIndex: index });
    this.updateEraLabel(index);
  },

  onRulerTouchEnd: function () {
    var index = this.data.eraIndex;
    if (index < 0) {
      index = Math.round(this.data.scrollLeft / SNAP_WIDTH);
      index = Math.min(Math.max(index, 0), this.data.eraOptions.length - 1);
    }
    this.snapToEra(index, true);
  },

  onCountTap: function (e) {
    var index = Number(e.detail.currentTarget.dataset.index);
    if (Number.isNaN(index)) return;
    this.setData({ countIndex: index });
  },

  onStart: function () {
    if (this.data.loading) return;

    var option =
      this.data.eraIndex >= 0
        ? this.data.eraOptions[this.data.eraIndex]
        : dynasties.ERA_ALL;
    var count = Number(this.data.countOptions[this.data.countIndex]) || config.defaultRoundCount;

    this.setData({ loading: true });
    var self = this;
    api
      .createSession({ era: option.key, count: count })
      .then(function (data) {
        var app = getApp();
        app.globalData.rounds = data.rounds || [];
        app.globalData.era = option.key;
        app.globalData.dynasty = "";
        app.globalData.sessionTotalScore = 0;
        app.globalData.sessionRoundScores = [];

        if (!app.globalData.rounds.length) {
          tt.showToast({ title: "暂无题目", icon: "none" });
          return;
        }

        tt.navigateTo({ url: "/pages/game/game?index=0" });
      })
      .catch(function (err) {
        tt.showToast({ title: err.message || "启动失败", icon: "none" });
      })
      .finally(function () {
        self.setData({ loading: false });
      });
  },
});
