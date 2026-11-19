package com.example.pharmora.exception;

public class ValidasiException extends RuntimeException {
    private final int kode;

    public ValidasiException(int kode, String pesan) {
        super(pesan);
        this.kode = kode;
    }

    public int getKode() {
        return kode;
    }
}