package com.example.pharmora.model;

import java.util.List;

public record Resep(String nama, String tglResep, String status, List<ItemObat> obat) {}