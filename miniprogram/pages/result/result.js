const dynasties = require("../../utils/dynasties.js");

Page({
  data: {
    roundIndex: 0,
    totalScore: 0,
    dynastyScore: 0,
    geoScore: 0,
    distanceKm: 0,
    guessDynasty: "",
    guessDynastyTitle: "",
    answerDynastyTitle: "",
    guessCity: "",
    answerCity: "",
    answer: null,
    nextLabel: "下一题",
  },

  onLoad: function (options) {
    var index = Number(options.index || 0);
    var app = getApp();
    var result = app.globalData.lastGuessResult;
    var isLast = index >= app.globalData.rounds.length - 1;

    if (!result) {
      tt.showToast({ title: "结果丢失", icon: "none" });
      tt.navigateBack();
      return;
    }

    this.setData({
      roundIndex: index,
      totalScore: result.totalScore,
      dynastyScore: result.dynastyScore,
      geoScore: result.geoScore,
      distanceKm: result.distanceKm,
      guessDynasty: result.guessDynasty || "",
      guessDynastyTitle: dynasties.titleOf(result.guessDynasty),
      answerDynastyTitle: dynasties.titleOf(result.answer && result.answer.dynasty),
      guessCity: result.guessCity || "",
      answerCity: result.answerCity || (result.answer && result.answer.modernPlace) || "",
      answer: result.answer,
      nextLabel: isLast ? "查看总成绩" : "下一题",
    });
  },

  onNext: function () {
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

  onHome: function () {
    tt.reLaunch({ url: "/pages/index/index" });
  },
});
