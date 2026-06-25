const config = require("../config.js");

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
  return new Promise(function (resolve, reject) {
    // 等待登录完成（无论成功失败），再执行业务请求
    // 跳过登录接口本身，避免死锁
    if (path === "/api/auth/login") {
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
    return data.dynasties || [];
  });
}

function createSession(options) {
  options = options || {};
  var query = "count=" + (options.count || 5);
  if (options.era) {
    query += "&era=" + encodeURIComponent(options.era);
  } else if (options.dynasty) {
    query += "&dynasty=" + encodeURIComponent(options.dynasty);
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
  var token = getToken();
  var query = "limit=" + limit + "&offset=" + offset;
  if (token) {
    query += "&token=" + encodeURIComponent(token);
  }
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
