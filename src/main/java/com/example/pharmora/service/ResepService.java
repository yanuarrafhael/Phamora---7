package com.example.pharmora.service;

import com.example.pharmora.exception.ValidasiException;
import com.example.pharmora.model.ItemObat;
import com.example.pharmora.model.Resep;
import com.example.pharmora.repository.ResepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ResepService {

    private static final Pattern POLA_ANGKA = Pattern.compile("^[0-9]+$");
    private static final Pattern POLA_TGL   = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final String ID_ADMIN = "AD001"; // sementara, nanti dari admin yang login

    private final ResepRepository repo;

    public ResepService(ResepRepository repo) {
        this.repo = repo;
    }

    public List<Map<String, Object>> daftarObat() {
        return repo.daftarObat();
    }

    @Transactional
    public void simpan(Resep req) {
        String nama   = bersihkan(req.nama());
        String tgl    = bersihkan(req.tglResep()).replace("/", "-");
        String status = bersihkan(req.status());

        // 1. Kolom kosong
        if (nama.isEmpty() || tgl.isEmpty() || status.isEmpty()) {
            throw new ValidasiException(400, "Lengkapi Data Terlebih Dahulu");
        }
        // 2. Tanggal
        if (!POLA_TGL.matcher(tgl).matches()) {
            throw new ValidasiException(400, "Format tanggal harus yyyy/mm/dd");
        }
        LocalDate tglResep;
        try {
            tglResep = LocalDate.parse(tgl, FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidasiException(400, "Tanggal tidak valid");
        }
        if (tglResep.isAfter(LocalDate.now())) {
            throw new ValidasiException(400, "Tanggal resep tidak boleh di masa depan");
        }
        // 3. Status
        if (status.equalsIgnoreCase("Selesai")) {
            status = "Selesai";
        } else if (status.equalsIgnoreCase("Belum")) {
            status = "Belum";
        } else {
            throw new ValidasiException(400, "Status harus Selesai atau Belum");
        }
        // 4. Pasien harus ada
        String idPasien = repo.cariIdPasien(nama);
        if (idPasien == null) {
            throw new ValidasiException(404, "Pasien tidak ditemukan");
        }
        // 5. Jumlah obat
        Map<String, Integer> dipilih = new LinkedHashMap<>();
        List<ItemObat> daftar = req.obat() == null ? List.of() : req.obat();
        for (ItemObat item : daftar) {
            String j = bersihkan(item.jumlah());
            if (j.isEmpty()) continue;
            if (!POLA_ANGKA.matcher(j).matches()) {
                throw new ValidasiException(400, "Jumlah harus angka positif");
            }
            if (j.length() > 6) {
                throw new ValidasiException(400, "Jumlah tidak valid");
            }
            int jml = Integer.parseInt(j);
            if (jml == 0) continue;
            dipilih.merge(item.idObat(), jml, Integer::sum);
        }
        if (dipilih.isEmpty()) {
            throw new ValidasiException(400, "Pilih minimal satu obat");
        }
        // 6. Stok mencukupi
        for (Map.Entry<String, Integer> e : dipilih.entrySet()) {
            ResepRepository.ObatStok o = repo.cariObat(e.getKey());
            if (o == null) {
                throw new ValidasiException(404, "Obat tidak ditemukan");
            }
            if (e.getValue() > o.stok()) {
                throw new ValidasiException(400, "Stok " + o.nama() + " tidak mencukupi");
            }
        }
        // 7. Simpan resep, detail, dan kurangi stok
        String idResep = repo.buatIdBaru();
        repo.simpanResep(idResep, idPasien, ID_ADMIN, tglResep, status);
        for (Map.Entry<String, Integer> e : dipilih.entrySet()) {
            repo.simpanDetail(idResep, e.getKey(), e.getValue());
            repo.kurangiStok(e.getKey(), e.getValue());
        }
    }

    private String bersihkan(String teks) {
        return teks == null ? "" : teks.trim();
    }
        public List<Map<String, Object>> daftarResep() {
        return repo.daftarResep();
    }

    @Transactional
    public void hapus(String id) {
        if (repo.hapusResep(id) == 0) {
            throw new ValidasiException(404, "Resep tidak ditemukan");
        }
    }
}