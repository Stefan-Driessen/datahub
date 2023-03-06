package com.linkedin.datahub.graphql.types.dataproduct.mappers;

// TODO: Check which imports are necessary
import com.linkedin.common.DataPlatformInstance;
import com.linkedin.common.Deprecation;
import com.linkedin.common.Embed;
import com.linkedin.common.GlobalTags;
import com.linkedin.common.GlossaryTerms;
import com.linkedin.common.InputFields;
import com.linkedin.common.InstitutionalMemory;
import com.linkedin.common.Ownership;
import com.linkedin.common.Status;
import com.linkedin.common.SubTypes;
import com.linkedin.common.urn.Urn;
// import com.linkedin.dashboard.EditableDashboardProperties; // Presumably not necessary
import com.linkedin.data.DataMap;
import com.linkedin.datahub.graphql.generated.AccessLevel;
import com.linkedin.datahub.graphql.generated.Chart;
import com.linkedin.datahub.graphql.generated.Container;
import com.linkedin.datahub.graphql.generated.DataProduct; // Presumably refers to \datahub\datahub-graphql-core\src\mainGeneratedGraphQL\java\com\linkedin\datahub\graphql\generated\DataProduct.java
// import com.linkedin.datahub.graphql.generated.DashboardEditableProperties; // Presumably not necessary
// import com.linkedin.datahub.graphql.generated.DashboardInfo; // Presumably not necessary
// import com.linkedin.datahub.graphql.generated.DashboardProperties; // Presumably not necessary
import com.linkedin.datahub.graphql.generated.DataPlatform;
import com.linkedin.datahub.graphql.generated.EntityType;
import com.linkedin.datahub.graphql.types.chart.mappers.InputFieldsMapper;
import com.linkedin.datahub.graphql.types.common.mappers.AuditStampMapper;
import com.linkedin.datahub.graphql.types.common.mappers.DataPlatformInstanceAspectMapper;
import com.linkedin.datahub.graphql.types.common.mappers.DeprecationMapper;
import com.linkedin.datahub.graphql.types.common.mappers.EmbedMapper;
import com.linkedin.datahub.graphql.types.common.mappers.InstitutionalMemoryMapper;
import com.linkedin.datahub.graphql.types.common.mappers.OwnershipMapper;
import com.linkedin.datahub.graphql.types.common.mappers.StatusMapper;
import com.linkedin.datahub.graphql.types.common.mappers.CustomPropertiesMapper;
import com.linkedin.datahub.graphql.types.common.mappers.util.MappingHelper;
import com.linkedin.datahub.graphql.types.common.mappers.util.SystemMetadataUtils;
import com.linkedin.datahub.graphql.types.domain.DomainAssociationMapper;
import com.linkedin.datahub.graphql.types.glossary.mappers.GlossaryTermsMapper;
import com.linkedin.datahub.graphql.types.mappers.ModelMapper;
import com.linkedin.datahub.graphql.types.tag.mappers.GlobalTagsMapper;
import com.linkedin.domain.Domains;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.EnvelopedAspectMap;
import com.linkedin.metadata.key.dataProductKey; // Presumably refers to \datahub\metadata-events\mxe-avro-1.7\src\generated\java\com\linkedin\pegasus2avro\metadata\key\dataProductKey.java
import com.linkedin.metadata.key.DataPlatformKey;
import com.linkedin.metadata.utils.EntityKeyUtils;
import java.util.stream.Collectors;
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
        mappingHelper.mapToResult(OWNERSHIP_ASPECT_NAME, (dashboard, dataMap) ->
            dashboard.setOwnership(OwnershipMapper.map(new Ownership(dataMap), entityUrn)));
        return mappingHelper.getResult();
    }

    private void mapDataProductKey(@Nonnull DataProduct dataproduct, @Nonnull DataMap dataMap) {
        final DataProductKey gmsKey = new dataProductKey(dataMap);
        dataproduct.setDomain(gmsKey.getDomain());
        dashboard.setId(gmsKey.getId());
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
