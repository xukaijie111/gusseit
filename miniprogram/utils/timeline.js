var dynasties = require("./dynasties.js");

var MIN_YEAR = -770;
var MAX_YEAR = 1912;
var TICKS_PER_SEGMENT = 10;
var CONTENT_WIDTH = 8680;

function _segments() {
  var periods = dynasties.periodsForTimeline(); // [{name, start, end}]
  var segmentWidth = periods.length ? Math.round(CONTENT_WIDTH / periods.length) : 0;
  var segments = [];
  var i;
  for (i = 0; i < periods.length; i++) {
    var p = periods[i];
    segments.push({
      name: p.name,
      start: p.start,
      end: p.end,
      left: i * segmentWidth,
      width: segmentWidth,
    });
  }
  return segments;
}

function yearToSegment(year) {
  var segments = _segments();
  if (!segments.length) return null;
  var i;
  for (i = 0; i < segments.length; i++) {
    var seg = segments[i];
    if (year >= seg.start && year <= seg.end) return seg;
  }
  if (year < segments[0].start) return segments[0];
  return segments[segments.length - 1];
}

function yearToOffset(year) {
  var seg = yearToSegment(year);
  if (!seg) return 0;
  var ratio = (year - seg.start) / (seg.end - seg.start);
  if (ratio < 0) ratio = 0;
  if (ratio > 1) ratio = 1;
  return seg.left + ratio * seg.width;
}

function offsetToYear(offset) {
  if (offset < 0) return MIN_YEAR;
  if (offset >= CONTENT_WIDTH) return MAX_YEAR;
  var segments = _segments();
  if (!segments.length) return defaultYear();
  var segmentWidth = segments[0].width;
  var index = Math.floor(offset / segmentWidth);
  if (index < 0) index = 0;
  if (index >= segments.length) index = segments.length - 1;
  var seg = segments[index];
  var ratio = (offset - seg.left) / seg.width;
  return Math.round(seg.start + ratio * (seg.end - seg.start));
}

function yearToScrollLeft(year) {
  return yearToOffset(year);
}

function formatYear(year) {
  if (year < 0) {
    return "公元前 " + Math.abs(year) + " 年";
  }
  return "公元 " + year + " 年";
}

function formatYearShort(year) {
  if (year < 0) {
    return "公元前 " + Math.abs(year);
  }
  return "公元 " + year;
}

function formatShortLabel(year) {
  if (year < 0) {
    return "前" + Math.abs(year);
  }
  return String(year);
}

function dynastyAt(year) {
  return dynasties.atYear(year);
}

function dynastyTitleAt(year) {
  return dynasties.titleAtYear(year);
}

function buildSegments() {
  return _segments();
}

function buildTicks() {
  var segments = _segments();
  var ticks = [];
  var i, j;
  for (i = 0; i < segments.length; i++) {
    var seg = segments[i];
    for (j = 0; j <= TICKS_PER_SEGMENT; j++) {
      var ratio = j / TICKS_PER_SEGMENT;
      var year = Math.round(seg.start + ratio * (seg.end - seg.start));
      var left = seg.left + ratio * seg.width;

      var isLong = (j === 0 || j === TICKS_PER_SEGMENT);
      var isMid = (j === 5);
      var isShort = (j % 2 === 0);
      var size = isLong ? "long" : isMid ? "mid" : isShort ? "short" : "mini";
      var label = j === 0 ? seg.name : "";

      ticks.push({
        year: year,
        left: left,
        size: size,
        label: label,
      });
    }
  }
  return ticks;
}

function contentWidthPx() {
  return CONTENT_WIDTH;
}

function defaultYear() {
  var segments = _segments();
  if (segments && segments.length) {
    var first = segments[0];
    var last = segments[segments.length - 1];
    return Math.round((first.start + last.end) / 2);
  }
  return Math.round((MIN_YEAR + MAX_YEAR) / 2);
}

module.exports = {
  MIN_YEAR: MIN_YEAR,
  MAX_YEAR: MAX_YEAR,
  formatYear: formatYear,
  formatYearShort: formatYearShort,
  yearToScrollLeft: yearToScrollLeft,
  offsetToYear: offsetToYear,
  dynastyAt: dynastyAt,
  dynastyTitleAt: dynastyTitleAt,
  buildSegments: buildSegments,
  buildTicks: buildTicks,
  contentWidthPx: contentWidthPx,
  defaultYear: defaultYear,
};
