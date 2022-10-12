package org.javacord.bot.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;

import java.io.IOException;

@ApplicationScoped
public class JavacordIconProvider {
    private byte[] icon;

    void readIcon(@Observes @Initialized(ApplicationScoped.class) Object unused) throws IOException {
        icon = getClass().getResourceAsStream("/javacord3_icon.png").readAllBytes();
    }

    public byte[] getIcon() {
        return icon;
    }
}
