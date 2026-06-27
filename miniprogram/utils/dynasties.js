/**
 * 朝代数据从后端 /api/game/dynasties 获取，game 页加载并缓存
 * FALLBACK 与后端 TimelineConstants 保持一致，兼容旧版 API 仅返回 id/name
 */

var ALL = { key: "", ruler: "全部", title: "全部朝代" };
var ERA_ALL = { key: "", ruler: "全部", title: "全部朝代", subtitle: "不限", dynastyIds: null };

var FALLBACK = [
  { key: "春秋", ruler: "春秋", title: "春秋", start: -770, end: -476, id: 1 },
  { key: "战国", ruler: "战国", title: "战国", start: -475, end: -221, id: 2 },
  { key: "秦", ruler: "秦", title: "秦朝", start: -221, end: -206, id: 3 },
  { key: "楚汉", ruler: "楚汉", title: "楚汉", start: -206, end: -202, id: 4 },
  { key: "西汉", ruler: "西汉", title: "西汉", start: -202, end: 8, id: 5 },
  { key: "东汉", ruler: "东汉", title: "东汉", start: 25, end: 220, id: 6 },
  { key: "三国", ruler: "三国", title: "三国", start: 220, end: 280, id: 7 },
  { key: "西晋", ruler: "西晋", title: "西晋", start: 265, end: 316, id: 8 },
  { key: "东晋", ruler: "东晋", title: "东晋", start: 317, end: 420, id: 9 },
  { key: "南北朝", ruler: "南北朝", title: "南北朝", start: 420, end: 589, id: 10 },
  { key: "隋", ruler: "隋", title: "隋朝", start: 581, end: 618, id: 11 },
  { key: "唐", ruler: "唐", title: "唐朝", start: 618, end: 907, id: 12 },
  { key: "五代十国", ruler: "五代", title: "五代十国", start: 907, end: 960, id: 13 },
  { key: "北宋", ruler: "北宋", title: "北宋", start: 960, end: 1127, id: 14 },
  { key: "南宋", ruler: "南宋", title: "南宋", start: 1127, end: 1279, id: 15 },
  { key: "元", ruler: "元", title: "元朝", start: 1271, end: 1368, id: 16 },
  { key: "明", ruler: "明", title: "明朝", start: 1368, end: 1644, id: 17 },
  { key: "清", ruler: "清", title: "清朝", start: 1644, end: 1912, id: 18 },
];

function _fallbackByName() {
  var map = {};
  var i;
  for (i = 0; i < FALLBACK.length; i++) {
    map[FALLBACK[i].key] = FALLBACK[i];
  }
  return map;
}

function _normalizeList(list) {
  if (!list || !list.length) return FALLBACK.slice();
  if (list[0].start != null && list[0].end != null) {
    return list.map(function (d) {
      return {
        key: d.key || d.name,
        ruler: d.ruler || d.name || d.key,
        title: d.title || d.name,
        start: d.start,
        end: d.end,
        id: d.id,
      };
    });
  }
  var byName = _fallbackByName();
  var out = [];
  var i;
  for (i = 0; i < list.length; i++) {
    var d = list[i];
    var base = byName[d.name] || byName[d.key];
    if (!base) continue;
    out.push({
      key: base.key,
      ruler: base.ruler,
      title: base.title,
      start: base.start,
      end: base.end,
      id: d.id != null ? d.id : base.id,
    });
  }
  return out.length ? out : FALLBACK.slice();
}

function _data() {
  try {
    var app = getApp();
    return app && app.globalData ? app.globalData.dynastiesData : null;
  } catch (e) {
    return null;
  }
}

function _dynasties() {
  var data = _data();
  return _normalizeList((data && data.dynasties) || []);
}

function _eras() {
  var data = _data();
  return (data && data.eras) || [];
}

function pickerOptions() {
  var options = [ALL];
  return options.concat(_dynasties());
}

function keys() {
  return _dynasties().map(function (d) {
    return d.key;
  });
}

function idOf(key) {
  var item = findByKey(key);
  return item && item.id != null ? item.id : null;
}

function findByKey(key) {
  if (!key) return ALL;
  var list = _dynasties();
  var i;
  for (i = 0; i < list.length; i++) {
    if (list[i].key === key) return list[i];
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
  var list = _dynasties();
  var i;
  for (i = 0; i < list.length; i++) {
    var d = list[i];
    if (year >= d.start && year <= d.end) return d.key;
  }
  return "";
}

function titleAtYear(year) {
  var key = atYear(year);
  return key ? titleOf(key) : "";
}

function periodsForTimeline() {
  return _dynasties().map(function (d) {
    return { name: d.ruler, start: d.start, end: d.end };
  });
}

function eraPickerOptions() {
  var options = [ERA_ALL];
  return options.concat(_eras());
}

function findEraByKey(key) {
  if (!key) return ERA_ALL;
  var list = _eras();
  var i;
  for (i = 0; i < list.length; i++) {
    if (list[i].key === key) return list[i];
  }
  return null;
}

module.exports = {
  ALL: ALL,
  LIST: [],
  ERA_ALL: ERA_ALL,
  ERAS: [],
  pickerOptions: pickerOptions,
  eraPickerOptions: eraPickerOptions,
  findEraByKey: findEraByKey,
  keys: keys,
  titleOf: titleOf,
  rulerOf: rulerOf,
  idOf: idOf,
  atYear: atYear,
  titleAtYear: titleAtYear,
  periodsForTimeline: periodsForTimeline,
};
