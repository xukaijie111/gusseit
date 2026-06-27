const config = require("../config.js");

var PUBLIC_PATHS = [
  "/api/game/dynasties",
  "/api/game/session",
  "/api/game/reverse-city",
  "/api/dynasties",
];

function isPublicPath(path) {
  var i;
  for (i = 0; i < PUBLIC_PATHS.length; i++) {
    if (path.indexOf(PUBLIC_PATHS[i]) === 0) return true;
  }
  return false;
}

function getToken() {
  try {
    var app = getApp();
    return app ? app.getToken() : "";
  } catch (e) {
    return "";
  }
}

function request(path, options) {
  options = options || {};
  var retryOnAuth = options.retryOnAuth !== false;
  return new Promise(function (resolve, reject) {
    // 等待登录完成（无论成功失败），再执行业务请求
    // 跳过登录接口本身，避免死锁
    if (path === "/api/auth/login" || isPublicPath(path)) {
      doRequest();
    } else {
      waitForLogin(doRequest);
    }
    function doRequest() {
      var headers = Object.assign({ "Content-Type": "application/json" }, options.header || {});
      var token = getToken();
      if (token) {
        headers["Authorization"] = "Bearer " + token;
      }
      tt.request({
        url: config.apiBase + path,
        method: options.method || "GET",
        data: options.data || {},
        header: headers,
        success: function (res) {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(res.data);
          } else if (
            retryOnAuth &&
            res.statusCode === 401 &&
            path !== "/api/auth/login"
          ) {
            // 后端重启后内存 token 失效，清除旧 token 并重新登录后再试一次
            try {
              tt.removeStorageSync("guseeit_token");
              var app = getApp();
              if (app) app.globalData.token = "";
            } catch (e) {}
            reloginOnce(function () {
              request(path, Object.assign({}, options, { retryOnAuth: false }))
                .then(resolve)
                .catch(reject);
            }, reject);
          } else {
            var msg = (res.data && res.data.error) || "请求失败";
            reject(new Error(msg));
          }
        },
        fail: function (err) {
          reject(err);
        },
      });
    }
  });
}

function reloginOnce(cb, failCb) {
  var app = getApp();
  tt.login({
    force: true,
    success: function (res) {
      if (!res.code) {
        failCb(new Error("未登录"));
        return;
      }
      tt.request({
        url: config.apiBase + "/api/auth/login",
        method: "POST",
        data: { code: res.code },
        header: { "Content-Type": "application/json" },
        success: function (loginRes) {
          if (loginRes.statusCode === 200 && loginRes.data && loginRes.data.token) {
            if (app) app.globalData.token = loginRes.data.token;
            tt.setStorageSync("guseeit_token", loginRes.data.token);
            cb();
          } else {
            failCb(new Error((loginRes.data && loginRes.data.error) || "未登录"));
          }
        },
        fail: function () {
          failCb(new Error("未登录"));
        },
      });
    },
    fail: function () {
      failCb(new Error("未登录"));
    },
  });
}

function waitForLogin(cb) {
  var app = getApp();
  if (!app || !app.waitForLogin) {
    cb();
    return;
  }
  app.waitForLogin(cb);
}

function fetchDynasties() {
  return request("/api/game/dynasties").then(function (data) {
    return data || {};
  });
}

function createSession(options) {
  options = options || {};
  var query = "count=" + (options.count || 5);
  if (options.era) {
    query += "&era=" + encodeURIComponent(options.era);
  } else if (options.dynastyId) {
    query += "&dynastyId=" + encodeURIComponent(options.dynastyId);
  }
  var token = getToken();
  if (token) {
    query += "&token=" + encodeURIComponent(token);
  }
  return request("/api/game/session?" + query);
}

function submitGuess(payload) {
  return request("/api/game/guess", { method: "POST", data: payload });
}

function reverseCity(latitude, longitude) {
  return request(
    "/api/game/reverse-city?latitude=" + latitude + "&longitude=" + longitude
  );
}

function login(code) {
  return request("/api/auth/login", {
    method: "POST",
    data: { code: code },
  });
}

function fetchHistory(offset, limit) {
  limit = limit || 20;
  offset = offset || 0;
  var query = "limit=" + limit + "&offset=" + offset;
  return request("/api/user/history?" + query);
}

module.exports = {
  request: request,
  fetchDynasties: fetchDynasties,
  createSession: createSession,
  submitGuess: submitGuess,
  reverseCity: reverseCity,
  login: login,
  fetchHistory: fetchHistory,
};
