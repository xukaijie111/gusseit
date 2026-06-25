// 模拟器可用 127.0.0.1；真机调试必须改为电脑局域网 IP（如 http://192.168.1.8:8787），且手机与电脑同 Wi‑Fi
// 上线改为 HTTPS 域名并在抖音后台配置 request 合法域名
module.exports = {
  apiBase: "http://127.0.0.1:8787",
  defaultRoundCount: 5,
  homeVideoSrc:
    "https://xkjpicture.oss-cn-beijing.aliyuncs.com/guseeit/home/home-bg.mp4",
};
