var toggle = document.querySelector(".toggle");   // ganti dengan class toggle-mu
var label  = document.querySelector(".label-tema"); // teks "Terang"/"Gelap"

function perbarui() {
  var gelap = document.documentElement.getAttribute("data-tema") === "gelap";
  if (label) label.textContent = gelap ? "Gelap" : "Terang";
}
if (toggle) {
  toggle.addEventListener("click", function () {
    var gelap = document.documentElement.getAttribute("data-tema") === "gelap";
    setTema(gelap ? "terang" : "gelap");
    perbarui();
  });
}
perbarui();