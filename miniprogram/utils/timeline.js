var dynasties = require("./dynasties.js");

var MIN_YEAR = -221;
var MAX_YEAR = 1949;
var PX_PER_YEAR = 4;

var PERIODS = dynasties.periodsForTimeline();

function formatYear(year) {
  if (year < 0) {
    return "公元前 " + Math.abs(year) + " 年";
  }
  return "公元 " + year + " 年";
}

function formatShortLabel(year) {
  if (year < 0) {
    return "前" + Math.abs(year);
  }
  return String(year);
}

function formatYearShort(year) {
  if (year < 0) {
    return "公元前 " + Math.abs(year);
  }
  return "公元 " + year;
}

function yearToOffset(year) {
  return (year - MIN_YEAR) * PX_PER_YEAR;
}

function offsetToYear(offset) {
  var y = MIN_YEAR + Math.round(offset / PX_PER_YEAR);
  if (y < MIN_YEAR) return MIN_YEAR;
  if (y > MAX_YEAR) return MAX_YEAR;
  return y;
}

function yearToScrollLeft(year) {
  return yearToOffset(year);
}

function dynastyAt(year) {
  return dynasties.atYear(year);
}

function dynastyTitleAt(year) {
  return dynasties.titleAtYear(year);
}

function buildSegments() {
  return PERIODS.map(function (p) {
    return {
      name: p.name,
      left: yearToOffset(p.start),
      width: yearToOffset(p.end) - yearToOffset(p.start),
    };
  });
}

function buildTicks() {
  var ticks = [];
  var y;
  for (y = MIN_YEAR; y <= MAX_YEAR; y++) {
    var isMini = y % 5 === 0;
    var isShort = y % 10 === 0;
    var isMid = y % 50 === 0;
    var isLong = y % 100 === 0 || y === MIN_YEAR || y === MAX_YEAR;
    if (!isMini) continue;

    var size = "mini";
    var label = "";
    if (isLong) {
      size = "long";
      label = formatShortLabel(y);
    } else if (isMid) {
      size = "mid";
    } else if (isShort) {
      size = "short";
    }

    ticks.push({
      year: y,
      left: yearToOffset(y),
      size: size,
      label: label,
    });
  }
  return ticks;
}

function contentWidthPx() {
  return (MAX_YEAR - MIN_YEAR) * PX_PER_YEAR;
}

function defaultYear() {
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
