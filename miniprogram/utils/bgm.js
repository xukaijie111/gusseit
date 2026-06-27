var config = require("../config.js");

function createBgm() {
  if (!config.bgmUrl) return null;

  var audio = tt.createInnerAudioContext();
  audio.src = config.bgmUrl;
  audio.loop = true;
  audio.volume = config.bgmVolume != null ? config.bgmVolume : 0.12;
  audio.obeyMuteSwitch = false;

  audio.onError(function (err) {
    console.warn("bgm error", err);
  });

  return audio;
}

function playBgm(app) {
  if (!app.globalData.bgm) return;
  try {
    app.globalData.bgm.play();
  } catch (e) {}
}

function pauseBgm(app) {
  if (!app.globalData.bgm) return;
  try {
    app.globalData.bgm.pause();
  } catch (e) {}
}

function initBgm(app) {
  if (app.globalData.bgm || !config.bgmUrl) return;

  app.globalData.bgm = createBgm();
  app.globalData.bgmPausedByHide = false;

  playBgm(app);

  if (!app._bgmLifecycleBound) {
    app._bgmLifecycleBound = true;
    tt.onAppShow(function () {
      if (app.globalData.bgmPausedByHide) {
        playBgm(app);
        app.globalData.bgmPausedByHide = false;
      }
    });
    tt.onAppHide(function () {
      if (app.globalData.bgm && !app.globalData.bgm.paused) {
        pauseBgm(app);
        app.globalData.bgmPausedByHide = true;
      }
    });
  }
}

module.exports = {
  initBgm: initBgm,
  playBgm: playBgm,
  pauseBgm: pauseBgm,
};
