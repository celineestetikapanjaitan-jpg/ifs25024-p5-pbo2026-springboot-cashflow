package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25024springbootcashflow.adapter.storage.FileStorage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Komponen upload gambar tunggal. File ditahan di memori sampai form disimpan,
 * lalu dipindahkan ke {@link FileStorage} lewat {@link #storeIn(FileStorage)}.
 */
class UploadField extends Div {

    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;

    private record PendingFile(String fileName, byte[] data) {
    }

    private final AtomicReference<PendingFile> pending = new AtomicReference<>();

    UploadField(String label) {
        Upload upload = new Upload(UploadHandler.inMemory(
                (metadata, data) -> pending.set(new PendingFile(metadata.fileName(), data))));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) MAX_FILE_SIZE);
        upload.getElement().addEventListener("file-remove", event -> pending.set(null));
        add(new Span(label), upload);
    }

    /** Menyimpan file yang sudah diunggah ke storage; null bila tidak ada file. */
    String storeIn(FileStorage storage) throws IOException {
        PendingFile file = pending.getAndSet(null);
        if (file == null) {
            return null;
        }
        return storage.save(file.fileName(), new ByteArrayInputStream(file.data()));
    }
}
