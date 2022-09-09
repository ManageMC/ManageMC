package com.managemc.api.wrapper.model.metadata;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Hard-coded constants of great significance. Do not change!
 */
public enum AuthMetadataType {
  @SerializedName("user_minecraft")
  PLAYER("user_minecraft"),
  @SerializedName("external_minecraft")
  EXTERNAL("external_minecraft"),
  @SerializedName("internal_minecraft")
  INTERNAL("internal_minecraft"),
  ;

  @Getter
  private final String type;

  AuthMetadataType(String type) {
    this.type = type;
  }
}
