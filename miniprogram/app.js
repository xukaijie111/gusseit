App({
  globalData: {
    rounds: [],
    dynasty: "",
    sessionTotalScore: 0,
    sessionRoundScores: [],
    token: "",
    loginReady: false,
    dynastiesData: null,
  },

  onLaunch: function () {
    var self = this;
    var saved = tt.getStorageSync("guseeit_token");
    if (saved) {
      self.globalData.token = saved;
    }
    // 登录最多阻塞 3 秒，避免业务请求一直 pending
    setTimeout(function () {
      if (!self.globalData.loginReady) {
        self.globalData.loginReady = true;
      }
    }, 3000);
    tt.login({
      force: false,
      success: function (res) {
        if (res.code) {
          self._loginWithCode(res.code);
        } else if (!self.globalData.loginReady) {
          self.globalData.loginReady = true;
        }
      },
      fail: function () {
        if (!self.globalData.loginReady) {
          self.globalData.loginReady = true;
        }
      },
    });
  },

  _loginWithCode: function (code) {
    var self = this;
    tt.request({
      url: self._apiBase() + "/api/auth/login",
      method: "POST",
      data: { code: code },
      header: { "Content-Type": "application/json" },
      success: function (res) {
        if (res.statusCode === 200 && res.data && res.data.token) {
          self.globalData.token = res.data.token;
          tt.setStorageSync("guseeit_token", res.data.token);
        }
        self.globalData.loginReady = true;
      },
      fail: function () {
        self.globalData.loginReady = true;
      },
    });
  },

  _apiBase: function () {
    var config = require("./config.js");
    return config.apiBase;
  },

  getToken: function () {
    return this.globalData.token || "";
  },

  waitForLogin: function (cb) {
    var self = this;
    if (self.globalData.loginReady) {
      cb();
      return;
    }
    var maxWait = 100;
    var interval = setInterval(function () {
      if (self.globalData.loginReady) {
        clearInterval(interval);
        cb();
      } else {
        maxWait--;
        if (maxWait <= 0) {
          clearInterval(interval);
          cb();
        }
      }
    }, 50);
  },
});
