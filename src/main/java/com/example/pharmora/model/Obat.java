package com.example.pharmora.model;

public record Obat(String nama, String idKategori, String stok,
                   String tglMasuk, String tglKeluar, String tglExpired, String deskripsi) {}