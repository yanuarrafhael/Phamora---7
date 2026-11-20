(function () {
  var t = "terang";
  try { t = localStorage.getItem("tema") || "terang"; } catch (e) {}
  document.documentElement.setAttribute("data-tema", t);
})();