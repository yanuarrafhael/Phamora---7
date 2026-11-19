const MENU = [
  ["Dashboard", "#", '<path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/>'],
  ["Data Obat", "#", '<rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4V3h6v1M9 17v-4M12 17v-6M15 17v-3"/>'],
  ["Kategori", "#", '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>'],
  ["Supplier", "menusupplier.html", '<path d="M12 3l8 4v10l-8 4-8-4V7z"/><path d="M4 7l8 4 8-4M12 11v10"/>'],
  ["Resep", "menuresep.html", '<path d="M6 3h9l4 4v14H6z"/><path d="M9 17V10h3a2 2 0 0 1 0 4H9M12 14l3 3"/>'],
  ["Transaksi", "#", '<circle cx="12" cy="12" r="9"/><path d="M12 7v10M15 9.5c0-1-1.3-1.8-3-1.8s-3 .8-3 1.8 1.3 1.6 3 2 3 1 3 2-1.3 1.8-3 1.8-3-.8-3-1.8"/>'],
  ["Laporan", "#", '<path d="M6 3h9l4 4v14H6z"/><path d="M9 12h7M9 16h7"/>'],
  ["Pasien", "menupasien.html", '<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 4-6 8-6s8 2 8 6"/>'],
  ["Pengaturan", "#", '<circle cx="12" cy="12" r="3"/><circle cx="12" cy="12" r="8" stroke-dasharray="4 3"/>']
];

const HALAMAN_INI = location.pathname.split("/").pop();

const menuHtml = MENU.map(m => {
  const kelas = [];
  if (m[0] === "Transaksi") kelas.push("celah");
  if (m[1] === HALAMAN_INI) kelas.push("aktif");
  return `<a href="${m[1]}" class="${kelas.join(' ')}"><svg viewBox="0 0 24 24">${m[2]}</svg>${m[0]}</a>`;
}).join("");

document.getElementById("sidebar").innerHTML = `
  <div class="logo-kotak"><img id="logo" src="logo2.png" alt="Pharmora"></div>
  <nav>${menuHtml}</nav>
  <div class="akun" id="akun">
    <div class="akun-kotak" onclick="bukaAkun()">
      <svg class="avatar" viewBox="0 0 24 24" fill="none" stroke="#006B63" stroke-width="2">
        <circle cx="12" cy="12" r="10"/><circle cx="12" cy="10" r="3.5" fill="#006B63"/>
        <path d="M5.5 19c1.5-3 4-4 6.5-4s5 1 6.5 4" fill="#006B63"/>
      </svg>
      <div class="akun-info">
        <div class="akun-nama">Haechan Lee</div>
        <div class="akun-peran">admin</div>
      </div>
      <div class="panah"></div>
    </div>
    <div class="akun-menu"><a href="#">Keluar</a></div>
  </div>`;

/* Logo: logo2.png -> logo-putih.png -> logo.png (diputihkan) -> teks */
const DAFTAR_LOGO = ["logo2.png", "logo-putih.png", "logo.png"];
let urutanLogo = 0;
const logo = document.getElementById("logo");
logo.onerror = function () {
  urutanLogo++;
  if (urutanLogo < DAFTAR_LOGO.length) {
    if (DAFTAR_LOGO[urutanLogo] === "logo.png") logo.classList.add("putihkan");
    logo.src = DAFTAR_LOGO[urutanLogo];
  } else {
    logo.onerror = null;
    logo.replaceWith(Object.assign(document.createElement("div"), {
      textContent: "Pharmora", style: "color:#fff;font-size:36px;font-weight:600"
    }));
  }
};

function bukaAkun() {
  document.getElementById("akun").classList.toggle("buka");
}