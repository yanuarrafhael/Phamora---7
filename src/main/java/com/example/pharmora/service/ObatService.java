package com.example.pharmora.service;

import com.example.pharmora.exception.ValidasiException;
import com.example.pharmora.model.Obat;
import com.example.pharmora.repository.ObatRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ObatService {

    private static final Pattern POLA_NAMA  = Pattern.compile("^[\\p{L}0-9 .,&()+\\-/]+$");
    private static final Pattern POLA_ANGKA = Pattern.compile("^[0-9]+$");
    private static final Pattern POLA_TGL   = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$");
    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final ObatRepository repo;

    public ObatService(ObatRepository repo) {
        this.repo = repo;
    }

    public List<Map<String, Object>> daftar() { return repo.daftar(); }

    public List<Map<String, Object>> kategori() { return repo.daftarKategori(); }

    public Map<String, Object> ambil(String id) {
        Map<String, Object> o = repo.cari(id);
        if (o == null) throw new ValidasiException(404, "Obat tidak ditemukan");
        return o;
    }

    private LocalDate tanggal(String teks, boolean wajib) {
        if (teks.isEmpty()) return null;
        String t = teks.replace("/", "-");
        if (!POLA_TGL.matcher(t).matches()) {
            throw new ValidasiException(400, "Format tanggal harus dd/mm/yyyy");
        }
        try {
            return LocalDate.parse(t, FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidasiException(400, "Tanggal tidak valid");
        }
    }

    private record Valid(String nama, String kat, int stok, LocalDate masuk, LocalDate keluar,
                         LocalDate exp, String desk) {}

    private Valid validasi(Obat req) {
        String nama = b(req.nama()), kat = b(req.idKategori()), stok = b(req.stok());
        String tm = b(req.tglMasuk()), tk = b(req.tglKeluar()), te = b(req.tglExpired()), desk = b(req.deskripsi());

        if (nama.isEmpty() || kat.isEmpty() || stok.isEmpty() || tm.isEmpty() || te.isEmpty() || desk.isEmpty()) {
            throw new ValidasiException(400, "Lengkapi Data Terlebih Dahulu");
        }
        if (!POLA_NAMA.matcher(nama).matches()) {
            throw new ValidasiException(400, "Nama obat tidak valid");
        }
        if (!POLA_ANGKA.matcher(stok).matches()) {
            throw new ValidasiException(400, "Stok harus angka");
        }
        if (stok.length() > 6) {
            throw new ValidasiException(400, "Stok terlalu besar");
        }
        LocalDate masuk = tanggal(tm, true);
        LocalDate keluar = tanggal(tk, false);
        LocalDate exp = tanggal(te, true);
        if (exp.isBefore(masuk) || exp.isEqual(masuk)) {
            throw new ValidasiException(400, "Tanggal kedaluwarsa harus setelah tanggal masuk");
        }
        if (keluar != null && keluar.isBefore(masuk)) {
            throw new ValidasiException(400, "Tanggal keluar tidak boleh sebelum tanggal masuk");
        }
        if (!repo.kategoriAda(kat)) {
            throw new ValidasiException(400, "Kategori tidak valid");
        }
        return new Valid(nama, kat, Integer.parseInt(stok), masuk, keluar, exp, desk);
    }

    public void simpan(Obat req) {
        Valid v = validasi(req);
        if (repo.namaAda(v.nama(), "")) throw new ValidasiException(409, "Obat sudah terdaftar");
        repo.simpan(repo.buatIdBaru(), v.nama(), v.kat(), v.stok(), v.masuk(), v.keluar(), v.exp(), v.desk());
    }

    public void ubah(String id, Obat req) {
        Valid v = validasi(req);
        if (repo.cari(id) == null) throw new ValidasiException(404, "Obat tidak ditemukan");
        if (repo.namaAda(v.nama(), id)) throw new ValidasiException(409, "Obat sudah terdaftar");
        repo.ubah(id, v.nama(), v.kat(), v.stok(), v.masuk(), v.keluar(), v.exp(), v.desk());
    }

    public void hapus(String id) {
        int n;
        try {
            n = repo.hapus(id);
        } catch (DataAccessException e) {
            throw new ValidasiException(409, "Obat masih dipakai data resep");
        }
        if (n == 0) throw new ValidasiException(404, "Obat tidak ditemukan");
    }

    private String b(String s) { return s == null ? "" : s.trim(); }
}