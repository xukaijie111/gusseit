const config = require("../../config.js");
const api = require("../../utils/api.js");
const bgm = require("../../utils/bgm.js");
const homeBgSrc = require("../../assets/home-bg.js");

Page({
  data: {
    homeBgSrc: homeBgSrc,
    countOptions: ["3", "5", "8"],
    countIndex: 1,
    loading: false,
  },

  onShow: function () {
    bgm.playBgm(getApp());
  },

  onCountTap: function (e) {
    bgm.playBgm(getApp());
    var index = Number(e.currentTarget.dataset.index);
    if (Number.isNaN(index)) return;
    this.setData({ countIndex: index });
  },

  onStart: function () {
    if (this.data.loading) return;
    bgm.playBgm(getApp());

    var count =
      Number(this.data.countOptions[this.data.countIndex]) ||
      config.defaultRoundCount;

    this.setData({ loading: true });
    var self = this;
    api
      .createSession({ count: count })
      .then(function (data) {
        var app = getApp();
        app.globalData.rounds = data.rounds || [];
        app.globalData.era = "";
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

  onHistory: function () {
    tt.navigateTo({ url: "/pages/history/history" });
  },
});
