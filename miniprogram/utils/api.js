const config = require("../config.js");

function request(path, options) {
  options = options || {};
  return new Promise(function (resolve, reject) {
    tt.request({
      url: config.apiBase + path,
      method: options.method || "GET",
      data: options.data || {},
      header: Object.assign({ "Content-Type": "application/json" }, options.header || {}),
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
  });
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

module.exports = {
  request: request,
  fetchDynasties: fetchDynasties,
  createSession: createSession,
  submitGuess: submitGuess,
  reverseCity: reverseCity,
};
