package com.futura.game.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SirenPlayer {
    private static final float SAMPLE_RATE = 22050.0f;
    private static final int DURATION_MS = 2000;
    private static final int CHUNK_MS = 200;
    private static final AtomicBoolean PLAYING = new AtomicBoolean(false);

    private SirenPlayer() {
    }

    public static void playWarningAsync() {
        if (!PLAYING.compareAndSet(false, true)) {
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                playSiren();
            } finally {
                PLAYING.set(false);
            }
        }, "dragon-siren");
        thread.setDaemon(true);
        thread.start();
    }

    private static void playSiren() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();

            int samplesPerChunk = (int) (SAMPLE_RATE * CHUNK_MS / 1000.0);
            byte[] buffer = new byte[samplesPerChunk * 2];
            int iterations = DURATION_MS / CHUNK_MS;

            for (int i = 0; i < iterations; i++) {
                double frequency = (i % 2 == 0) ? 880.0 : 620.0;
                fillTone(buffer, frequency);
                line.write(buffer, 0, buffer.length);
            }

            line.drain();
        } catch (LineUnavailableException ignored) {
        }
    }

    private static void fillTone(byte[] buffer, double frequency) {
        for (int i = 0; i < buffer.length / 2; i++) {
            double angle = 2.0 * Math.PI * i * frequency / SAMPLE_RATE;
            short sample = (short) (Math.sin(angle) * Short.MAX_VALUE * 0.12);
            buffer[i * 2] = (byte) (sample & 0xff);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
        }
    }
}
