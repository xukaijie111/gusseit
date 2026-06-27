// 本地调试: isDev=true，apiBase 指向本机
// 上线/云端: isDev=false，并在抖音后台配置 request 合法域名 api.agentnow.fun
var isDev = false;

module.exports = {
  apiBase: isDev ? "http://127.0.0.1:8787" : "https://api.agentnow.fun",
  defaultRoundCount: 5,
};
