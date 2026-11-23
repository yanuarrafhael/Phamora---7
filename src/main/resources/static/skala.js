(function () {
  const BASE_LEBAR = 1535;  // lebar layar acuan (layarmu sekarang)
  const BASE_ZOOM  = 0.75;  // ukuran yang sudah pas di layar acuan

  function atur() {
    const z = BASE_ZOOM * window.innerWidth / BASE_LEBAR;
    document.body.style.zoom = z;
    document.documentElement.style.setProperty("--zoom", z);
  }
  window.addEventListener("resize", atur);
  atur();
})();