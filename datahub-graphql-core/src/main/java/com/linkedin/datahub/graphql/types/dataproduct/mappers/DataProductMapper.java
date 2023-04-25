package com.linkedin.datahub.graphql.types.dataproduct.mappers;

import com.linkedin.common.Ownership;
import com.linkedin.common.urn.Urn;
import com.linkedin.data.DataMap;
import com.linkedin.data.template.GetMode;
import com.linkedin.datahub.graphql.generated.DataProduct; // Presumably refers to \datahub\datahub-graphql-core\src\mainGeneratedGraphQL\java\com\linkedin\datahub\graphql\generated\DataProduct.java
import com.linkedin.datahub.graphql.generated.EntityType;
import com.linkedin.datahub.graphql.types.common.mappers.OwnershipMapper;
import com.linkedin.datahub.graphql.types.common.mappers.util.MappingHelper;
import com.linkedin.datahub.graphql.types.mappers.ModelMapper;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.EnvelopedAspectMap;
import com.linkedin.metadata.key.DataProductKey; // I can't find this!
import com.linkedin.dataproduct.DataProductProperties;
import javax.annotation.Nonnull;

import static com.linkedin.metadata.Constants.*;


public class DataProductMapper implements ModelMapper<EntityResponse, DataProduct> {

    public static final DataProductMapper INSTANCE = new DataProductMapper();

    public static DataProduct map(@Nonnull final EntityResponse entityResponse) {
        return INSTANCE.apply(entityResponse);
    }

    @Override
    public DataProduct apply(@Nonnull final EntityResponse entityResponse) {
        System.out.println("Do you even map bro?");
        final DataProduct result = new DataProduct();
        Urn entityUrn = entityResponse.getUrn();

        result.setUrn(entityResponse.getUrn().toString());
        result.setType(EntityType.DATA_PRODUCT);
        EnvelopedAspectMap aspectMap = entityResponse.getAspects();
        System.out.println(String.format("25041507 The aspect name constant is: %s", DATA_PRODUCT_PROPERTIES_ASPECT_NAME));
        System.out.println(aspectMap.containsKey(DATA_PRODUCT_PROPERTIES_ASPECT_NAME));
        System.out.println("Can I figure out what the aspectnames are?");
        System.out.println(aspectMap.keySet());
        System.out.println(aspectMap.data());
        // System.out.println(aspectMap.get(DATA_PRODUCT_PROPERTIES_ASPECT_NAME).getValue().data());
        MappingHelper<DataProduct> mappingHelper = new MappingHelper<>(aspectMap, result);
        System.out.println("Defined a MappingHelper");
        mappingHelper.mapToResult(DATA_PRODUCT_KEY_ASPECT_NAME, this::mapDataProductKey);
        System.out.println("Mapped Key aspect");
        mappingHelper.mapToResult(DATA_PRODUCT_PROPERTIES_ASPECT_NAME, this::mapDataProductProperties);
        System.out.println("'Mapped DataProduct Properties'");
        mappingHelper.mapToResult(OWNERSHIP_ASPECT_NAME, (dataproduct, dataMap) ->
            dataproduct.setOwnership(OwnershipMapper.map(new Ownership(dataMap), entityUrn)));
        System.out.println("But I'm not getting here for some reason?");

        if (result.getProperties() != null && result.getProperties().getName() == null) {
            result.getProperties().setName("I have to figure out if this is relevant and how I want to deal with this.");
        }

        return mappingHelper.getResult();
    }

    private void mapDataProductKey(@Nonnull DataProduct dataproduct, @Nonnull DataMap dataMap) {
        final DataProductKey gmsKey = new DataProductKey(dataMap);
        dataproduct.setDomain(gmsKey.getDomain());
        dataproduct.setName(gmsKey.getName());
    }

    private void mapDataProductProperties(@Nonnull DataProduct dataProduct, @Nonnull DataMap dataMap) {
        System.out.println("mapDataProperties is called!");
        final DataProductProperties properties = new DataProductProperties(dataMap);
        System.out.println(String.format("properties.Name():", properties.getName()));
        final com.linkedin.datahub.graphql.generated.DataProductProperties graphQlProperties =
            new com.linkedin.datahub.graphql.generated.DataProductProperties.Builder()
                .setName(properties.getName(GetMode.DEFAULT))
                .setDescription(properties.getDescription(GetMode.DEFAULT))
                .build();
        System.out.println(String.format(
            "I should have just mapped the name (%s) and description (%s) of the data product properties.", 
            properties.getName(), properties.getDescription()));
        dataProduct.setProperties(graphQlProperties);
    }
}
