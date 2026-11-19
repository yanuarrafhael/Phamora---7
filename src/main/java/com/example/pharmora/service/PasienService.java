package com.example.pharmora.service;

import com.example.pharmora.exception.ValidasiException;
import com.example.pharmora.model.Pasien;
import com.example.pharmora.repository.PasienRepository;
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
public class PasienService {

    private static final Pattern POLA_NAMA  = Pattern.compile("^[\\p{L} .,'\\-]+$");
    private static final Pattern POLA_ANGKA = Pattern.compile("^[0-9]+$");
    private static final Pattern POLA_TGL   = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private record Valid(Pasien pasien, LocalDate tgl) {}

    private final PasienRepository repo;

    public PasienService(PasienRepository repo) {
        this.repo = repo;
    }

    public List<Map<String, Object>> daftar() {
        return repo.daftar();
    }

    public Map<String, Object> ambil(String id) {
        Map<String, Object> p = repo.cari(id);
        if (p == null) {
            throw new ValidasiException(404, "Pasien tidak ditemukan");
        }
        return p;
    }

    private Valid validasi(Pasien req) {
        String nama   = bersihkan(req.nama());
        String tgl    = bersihkan(req.tglLahir()).replace("/", "-");
        String jk     = bersihkan(req.jenisKelamin()).toUpperCase();
        String alamat = bersihkan(req.alamat());
        String hp     = bersihkan(req.hp());

        if (nama.isEmpty() || tgl.isEmpty() || jk.isEmpty() || alamat.isEmpty() || hp.isEmpty()) {
            throw new ValidasiException(400, "Lengkapi Data Terlebih Dahulu");
        }
        if (!POLA_NAMA.matcher(nama).matches()) {
            throw new ValidasiException(400, "Nama pasien hanya boleh huruf");
        }
        if (!POLA_TGL.matcher(tgl).matches()) {
            throw new ValidasiException(400, "Format tanggal harus yyyy/mm/dd");
        }
        LocalDate tglLahir;
        try {
            tglLahir = LocalDate.parse(tgl, FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidasiException(400, "Tanggal lahir tidak valid");
        }
        if (tglLahir.isAfter(LocalDate.now())) {
            throw new ValidasiException(400, "Tanggal lahir tidak boleh di masa depan");
        }
        if (tglLahir.isBefore(LocalDate.now().minusYears(120))) {
            throw new ValidasiException(400, "Tanggal lahir tidak valid");
        }
        if (!jk.equals("L") && !jk.equals("P")) {
            throw new ValidasiException(400, "Jenis kelamin harus L atau P");
        }
        if (!POLA_ANGKA.matcher(hp).matches()) {
            throw new ValidasiException(400, "No HP hanya boleh angka");
        }
        if (hp.length() < 10 || hp.length() > 13) {
            throw new ValidasiException(400, "No HP harus 10 sampai 13 digit");
        }
        return new Valid(new Pasien(nama, tgl, jk, alamat, hp), tglLahir);
    }

    public void simpan(Pasien req) {
        Valid v = validasi(req);
        if (repo.sudahAda(v.pasien().nama(), v.tgl())) {
            throw new ValidasiException(409, "Pasien sudah terdaftar");
        }
        repo.simpan(repo.buatIdBaru(), v.pasien(), v.tgl());
    }

    public void ubah(String id, Pasien req) {
        Valid v = validasi(req);
        if (repo.cari(id) == null) {
            throw new ValidasiException(404, "Pasien tidak ditemukan");
        }
        if (repo.sudahAdaSelain(v.pasien().nama(), v.tgl(), id)) {
            throw new ValidasiException(409, "Pasien sudah terdaftar");
        }
        repo.ubah(id, v.pasien(), v.tgl());
    }

    public void hapus(String id) {
        int terhapus;
        try {
            terhapus = repo.hapus(id);
        } catch (DataAccessException e) {
            throw new ValidasiException(409, "Pasien masih dipakai data resep");
        }
        if (terhapus == 0) {
            throw new ValidasiException(404, "Pasien tidak ditemukan");
        }
    }

    private String bersihkan(String teks) {
        return teks == null ? "" : teks.trim();
    }
}