package com.hedera.sdk.crypto;

import com.hedera.sdk.contract.ContractId;
import com.hedera.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.sdk.proto.ContractID;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.encoders.Hex;

public interface Key {
    com.hedera.sdk.proto.Key toKeyProto();

    static Key fromProtoKey(com.hedera.sdk.proto.Key key) {
        switch (key.getKeyCase()) {
        case ED25519:
            return Ed25519PublicKey.fromBytes(
                key.getEd25519()
                    .toByteArray()
            );
        case CONTRACTID:
            ContractID id = key.getContractID();
            return new ContractId(id.getShardNum(), id.getRealmNum(), id.getContractNum());
        default:
            throw new IllegalStateException("Unchecked Key Case");
        }
    }

    static Key fromString(String keyString) {
        SubjectPublicKeyInfo pubKeyInfo;

        try {
            var keyBytes = Hex.decode(keyString);
            pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse public key", e);
        }

        var algId = pubKeyInfo.getAlgorithm()
            .getAlgorithm();

        if (algId.equals(EdECObjectIdentifiers.id_Ed25519)) {
            return Ed25519PublicKey.fromBytes(
                pubKeyInfo.getPublicKeyData()
                    .getBytes()
            );
        } else {
            throw new IllegalArgumentException("Unsupported public key type: " + algId.toString());
        }
    }
}
