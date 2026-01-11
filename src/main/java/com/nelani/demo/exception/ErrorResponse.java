package com.nelani.demo.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "Represents an error response with a code and message")
public record ErrorResponse(
                @Schema(description = "Error code or type", example = "Validation Error") String error,

                @Schema(description = "Detailed error message", example = "The provided email does not match the registered email for this account.") String message

) {
}
