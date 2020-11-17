package no.nav.dokarkivavlevering.avlevering.consumer.sts;

import lombok.Getter;

@Getter
public class StsRequest {
    private final String grant_type = "client_credentials";
    private final String scope = "openid";
}