package com.example.pharmora.controller;

import com.example.pharmora.exception.ValidasiException;
import com.example.pharmora.model.Supplier;
import com.example.pharmora.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> daftar() {
        return service.daftar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> ambil(@PathVariable("id") String id) {
        try {
            return ResponseEntity.ok(service.ambil(id));
        } catch (ValidasiException e) {
            return ResponseEntity.status(e.getKode()).body(Map.of("pesan", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> simpan(@RequestBody Supplier req) {
        try {
            service.simpan(req);
            return ResponseEntity.ok(Map.of("pesan", "Supplier berhasil disimpan"));
        } catch (ValidasiException e) {
            return ResponseEntity.status(e.getKode()).body(Map.of("pesan", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("pesan", "Gagal menyimpan data"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> ubah(@PathVariable("id") String id, @RequestBody Supplier req) {
        try {
            service.ubah(id, req);
            return ResponseEntity.ok(Map.of("pesan", "Supplier berhasil diubah"));
        } catch (ValidasiException e) {
            return ResponseEntity.status(e.getKode()).body(Map.of("pesan", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("pesan", "Gagal mengubah data"));
        }
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