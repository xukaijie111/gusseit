/**
 * 与后端 DynastyConstants.SUPPORTED 保持一致
 * key: API 传参；ruler: 尺子上刻字；title: 展示用全称
 */
var ALL = {
  key: "",
  ruler: "全部",
  title: "全部朝代",
};

var LIST = [
  { key: "秦", ruler: "秦", title: "秦朝", start: -221, end: -206 },
  { key: "汉", ruler: "汉", title: "汉朝", start: -206, end: 220 },
  { key: "三国", ruler: "三国", title: "三国", start: 220, end: 280 },
  { key: "晋", ruler: "晋", title: "晋朝", start: 265, end: 420 },
  { key: "南北朝", ruler: "南北朝", title: "南北朝", start: 420, end: 589 },
  { key: "隋", ruler: "隋", title: "隋朝", start: 581, end: 618 },
  { key: "唐", ruler: "唐", title: "唐朝", start: 618, end: 907 },
  { key: "宋", ruler: "宋", title: "宋朝", start: 960, end: 1279 },
  { key: "元", ruler: "元", title: "元朝", start: 1271, end: 1368 },
  { key: "明", ruler: "明", title: "明朝", start: 1368, end: 1644 },
  { key: "清", ruler: "清", title: "清朝", start: 1644, end: 1912 },
  { key: "民国", ruler: "民国", title: "民国", start: 1912, end: 1949 },
];

function pickerOptions() {
  var options = [ALL];
  var i;
  for (i = 0; i < LIST.length; i++) {
    options.push(LIST[i]);
  }
  return options;
}

function keys() {
  return LIST.map(function (d) {
    return d.key;
  });
}

function findByKey(key) {
  if (!key) return ALL;
  var i;
  for (i = 0; i < LIST.length; i++) {
    if (LIST[i].key === key) return LIST[i];
  }
  return null;
}

function titleOf(key) {
  var item = findByKey(key);
  if (!item) return key ? key : ALL.title;
  return item.title;
}

function rulerOf(key) {
  var item = findByKey(key);
  if (!item) return key ? key : ALL.ruler;
  return item.ruler;
}

function atYear(year) {
  var i;
  for (i = 0; i < LIST.length; i++) {
    var d = LIST[i];
    if (year >= d.start && year <= d.end) return d.key;
  }
  return "";
}

function titleAtYear(year) {
  var key = atYear(year);
  return key ? titleOf(key) : "";
}

function periodsForTimeline() {
  return LIST.map(function (d) {
    return { name: d.ruler, start: d.start, end: d.end };
  });
}

/** 首页时代范围，与后端 EraConstants 一致 */
var ERA_ALL = {
  key: "",
  ruler: "全部",
  title: "全部朝代",
  subtitle: "不限",
  dynasties: null,
};

var ERAS = [
  {
    key: "proto",
    ruler: "远古",
    title: "远古",
    subtitle: "秦 · 汉",
    dynasties: ["秦", "汉"],
  },
  {
    key: "classic",
    ruler: "古代",
    title: "古代",
    subtitle: "三国至唐",
    dynasties: ["三国", "晋", "南北朝", "隋", "唐"],
  },
  {
    key: "empire",
    ruler: "近古",
    title: "近古",
    subtitle: "宋元至清",
    dynasties: ["宋", "元", "明", "清"],
  },
  {
    key: "modern",
    ruler: "近代",
    title: "近代",
    subtitle: "民国",
    dynasties: ["民国"],
  },
];

function eraPickerOptions() {
  var options = [ERA_ALL];
  var i;
  for (i = 0; i < ERAS.length; i++) {
    options.push(ERAS[i]);
  }
  return options;
}

function findEraByKey(key) {
  if (!key) return ERA_ALL;
  var i;
  for (i = 0; i < ERAS.length; i++) {
    if (ERAS[i].key === key) return ERAS[i];
  }
  return null;
}

module.exports = {
  ALL: ALL,
  LIST: LIST,
  ERA_ALL: ERA_ALL,
  ERAS: ERAS,
  pickerOptions: pickerOptions,
  eraPickerOptions: eraPickerOptions,
  findEraByKey: findEraByKey,
  keys: keys,
  titleOf: titleOf,
  rulerOf: rulerOf,
  atYear: atYear,
  titleAtYear: titleAtYear,
  periodsForTimeline: periodsForTimeline,
};
