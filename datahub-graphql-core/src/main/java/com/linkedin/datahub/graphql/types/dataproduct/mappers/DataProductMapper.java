package com.linkedin.datahub.graphql.types.dataproduct.mappers;

import com.linkedin.common.Ownership;
import com.linkedin.common.urn.Urn;
import com.linkedin.data.DataMap;
import com.linkedin.datahub.graphql.generated.DataProduct; // Presumably refers to \datahub\datahub-graphql-core\src\mainGeneratedGraphQL\java\com\linkedin\datahub\graphql\generated\DataProduct.java
import com.linkedin.datahub.graphql.generated.EntityType;
import com.linkedin.datahub.graphql.types.common.mappers.OwnershipMapper;
import com.linkedin.datahub.graphql.types.common.mappers.util.MappingHelper;
import com.linkedin.datahub.graphql.types.mappers.ModelMapper;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.EnvelopedAspectMap;
import com.linkedin.metadata.key.DataProductKey; // I can't find this!
import javax.annotation.Nonnull;

import static com.linkedin.metadata.Constants.*;


public class DataProductMapper implements ModelMapper<EntityResponse, DataProduct> {

    public static final DataProductMapper INSTANCE = new DataProductMapper();

    public static DataProduct map(@Nonnull final EntityResponse entityResponse) {
        return INSTANCE.apply(entityResponse);
    }

    @Override
    public DataProduct apply(@Nonnull final EntityResponse entityResponse) {
        final DataProduct result = new DataProduct();
        Urn entityUrn = entityResponse.getUrn();

        result.setUrn(entityResponse.getUrn().toString());
        result.setType(EntityType.DATA_PRODUCT);
        EnvelopedAspectMap aspectMap = entityResponse.getAspects();

        MappingHelper<DataProduct> mappingHelper = new MappingHelper<>(aspectMap, result);
        mappingHelper.mapToResult(DATA_PRODUCT_KEY_ASPECT_NAME, this::mapDataProductKey);
        mappingHelper.mapToResult(OWNERSHIP_ASPECT_NAME, (dataproduct, dataMap) ->
            dataproduct.setOwnership(OwnershipMapper.map(new Ownership(dataMap), entityUrn)));
        return mappingHelper.getResult();
    }

    private void mapDataProductKey(@Nonnull DataProduct dataproduct, @Nonnull DataMap dataMap) {
        final DataProductKey gmsKey = new DataProductKey(dataMap);
        dataproduct.setDomain(gmsKey.getDomain());
        dataproduct.setName(gmsKey.getName());
    }

    // Useful in future
    // private void mapGlobalTags(@Nonnull Dashboard dashboard, @Nonnull DataMap dataMap, @Nonnull Urn entityUrn) {
    //     com.linkedin.datahub.graphql.generated.GlobalTags globalTags = GlobalTagsMapper.map(new GlobalTags(dataMap), entityUrn);
    //     dashboard.setGlobalTags(globalTags);
    //     dashboard.setTags(globalTags);
    // }

    // Useful in future
    // private void mapDomains(@Nonnull Dashboard dashboard, @Nonnull DataMap dataMap) {
    //     final Domains domains = new Domains(dataMap);
    //     dashboard.setDomain(DomainAssociationMapper.map(domains, dashboard.getUrn()));
    // }
}
