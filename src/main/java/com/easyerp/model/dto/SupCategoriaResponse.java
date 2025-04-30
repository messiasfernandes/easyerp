package com.easyerp.model.dto;

import com.easyerp.domain.entidade.SubCategoria;

public record SupCategoriaResponse(Long id, String subcategoria) {
     public SupCategoriaResponse(SubCategoria subCategoria) {
        this(subCategoria.getId(), subCategoria.getSubcategoriaNome());    	 
     }
}
