package com.exceptional.musiccore.engine.metadata;

/**
 * Created by sebnapi on 18.03.14.
 * <p>
 * This interface needs to be implemented by new Metadata-Types
 * so Visitors can visit them and work with them
 */
public interface JXMetaAcceptor {
    <T> T accept(JXMetaVisitor<T> visitor);
}