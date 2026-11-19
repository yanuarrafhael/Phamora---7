package com.example.pharmora.controller;

import com.example.pharmora.exception.ValidasiException;
import com.example.pharmora.model.Resep;
import com.example.pharmora.service.ResepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/resep")
public class ResepController {

    private final ResepService service;

    public ResepController(ResepService service) {
        this.service = service;
    }

    @GetMapping("/obat")
    public List<Map<String, Object>> daftarObat() {
        return service.daftarObat();
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> simpan(@RequestBody Resep req) {
        try {
            service.simpan(req);
            return ResponseEntity.ok(Map.of("pesan", "Resep berhasil disimpan"));
        } catch (ValidasiException e) {
            return ResponseEntity.status(e.getKode()).body(Map.of("pesan", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("pesan", "Gagal menyimpan data"));
        }
    }
        @GetMapping
    public List<Map<String, Object>> daftar() {
        return service.daftarResep();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> hapus(@PathVariable("id") String id) {
        try {
            service.hapus(id);
            return ResponseEntity.ok(Map.of("pesan", "Data Berhasil Dihapus"));
        } catch (ValidasiException e) {
            return ResponseEntity.status(e.getKode()).body(Map.of("pesan", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("pesan", "Gagal menghapus data"));
        }
    }
}