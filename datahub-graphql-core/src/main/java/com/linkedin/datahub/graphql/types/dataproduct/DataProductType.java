package com.linkedin.datahub.graphql.types.dataproduct;
import com.google.common.collect.ImmutableSet;
// Presumably this revers to \datahub\li-utils\src\main\javaPegasus\com\linkedin\common\ urn\DashboardUrn.java and is not necessary.
// import com.linkedin.common.urn.DashboardUrn;
import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.DataProduct; // Presumably refers to \datahub\datahub-graphql-core\src\mainGeneratedGraphQL\java\com\linkedin\datahub\graphql\generated\DataProduct.java
// import com.linkedin.datahub.graphql.generated.DashboardUpdateInput; // Presumably not needed
import com.linkedin.datahub.graphql.generated.Entity;
import com.linkedin.datahub.graphql.generated.EntityType; // Presumably refers to \datahub\datahub-graphql-core\src\mainGeneratedGraphQL\java\com\linkedin\datahub\graphql\generated\EntityType.java
import com.linkedin.datahub.graphql.types.dataproduct.mappers.DataProductMapper; // File in same folder.
// import com.linkedin.datahub.graphql.types.dashboard.mappers.DashboardUpdateInputMapper; // Presumably not needed
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.metadata.Constants;
import graphql.execution.DataFetcherResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import static com.linkedin.datahub.graphql.Constants.*;
import static com.linkedin.metadata.Constants.*;

public class DataProductType implements com.linkedin.datahub.graphql.types.EntityType<DataProduct, String> {

    private static final Set<String> ASPECTS_TO_RESOLVE = ImmutableSet.of(
        // Maintained here: \datahub\li-utils\src\main\java\com\linkedin\metadata\Constants.java
        DATA_PRODUCT_KEY_ASPECT_NAME,
        DATA_PRODUCT_PROPERTIES_ASPECT_NAME,
        OWNERSHIP_ASPECT_NAME
    );
    
    private static final String ENTITY_NAME = "data product"; // Copied from DatasetType.java

    private final EntityClient _entityClient; // They all have this, no idea what it is.

    public DataProductType(final EntityClient entityClient) {
        _entityClient = entityClient;
    }

    @Override
    public EntityType type() {
        return EntityType.DATA_PRODUCT;
    }

    @Override
    public Function<Entity, String> getKeyProvider() {
        return Entity::getUrn;
    }

    @Override
    public Class<DataProduct> objectClass() {
        return DataProduct.class;
    }

    // All the classes seem to have this, currently not trying to batch load data products but too scared to remove this.
    @Override
    public List<DataFetcherResult<DataProduct>> batchLoad(@Nonnull List<String> urnStrs, @Nonnull QueryContext context) throws Exception {
        final List<Urn> urns = urnStrs.stream()
            .map(UrnUtils::getUrn)
            .collect(Collectors.toList());
        System.out.println("Defined urns");
        System.out.println(urns);
        
        try {
            final Map<Urn, EntityResponse> dataProductMap =
                _entityClient.batchGetV2(
                    Constants.DATA_PRODUCT_ENTITY_NAME, // Added here: \datahub\li-utils\src\main\java\com\linkedin\metadata\Constants.java
                    new HashSet<>(urns),
                    ASPECTS_TO_RESOLVE,
                    context.getAuthentication());

            final List<EntityResponse> gmsResults = new ArrayList<>();
            for (Urn urn : urns) {
                gmsResults.add(dataProductMap.getOrDefault(urn, null));
            }
            return gmsResults.stream()
                .map(gmsDataProduct -> gmsDataProduct == null ? null : DataFetcherResult.<DataProduct>newResult()
                    .data(DataProductMapper.map(gmsDataProduct))
                    .build())
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch load Data Product", e);
        }
    }
    // Useful for future 
    // @Override
    // public SearchResults search(@Nonnull String query,
    //                             @Nullable List<FacetFilterInput> filters,
    //                             int start,
    //                             int count,
    //                             @Nonnull QueryContext context) throws Exception {
    //     final Map<String, String> facetFilters = ResolverUtils.buildFacetFilters(filters, FACET_FIELDS);
    //     final SearchResult searchResult = _entityClient.search("dashboard", query, facetFilters, start, count,
    //             context.getAuthentication(), true, null);
    //     return UrnSearchResultsMapper.map(searchResult);
    // }

    // Useful for future
    // @Override
    // public AutoCompleteResults autoComplete(@Nonnull String query,
    //                                         @Nullable String field,
    //                                         @Nullable List<FacetFilterInput> filters,
    //                                         int limit,
    //                                         @Nonnull QueryContext context) throws Exception {
    //     final Map<String, String> facetFilters = ResolverUtils.buildFacetFilters(filters, FACET_FIELDS);
    //     final AutoCompleteResult result = _entityClient.autoComplete("dashboard", query, facetFilters, limit, context.getAuthentication());
    //     return AutoCompleteResultsMapper.map(result);
    // }

    // @Override
    // public BrowseResults browse(@Nonnull List<String> path,
    //                             @Nullable List<FacetFilterInput> filters,
    //                             int start, int count,
    //                             @Nonnull QueryContext context) throws Exception {
    //     final Map<String, String> facetFilters = ResolverUtils.buildFacetFilters(filters, FACET_FIELDS);
    //     final String pathStr = path.size() > 0 ? BROWSE_PATH_DELIMITER + String.join(BROWSE_PATH_DELIMITER, path) : "";
    //     final BrowseResult result = _entityClient.browse(
    //         "dashboard",
    //             pathStr,
    //             facetFilters,
    //             start,
    //             count,
    //         context.getAuthentication());
    //     return BrowseResultMapper.map(result);
    // }

    // Useful for future
    // @Override
    // public List<BrowsePath> browsePaths(@Nonnull String urn, @Nonnull QueryContext context) throws Exception {
    //     final StringArray result = _entityClient.getBrowsePaths(getDashboardUrn(urn), context.getAuthentication());
    //     return BrowsePathsMapper.map(result);
    // }

    // private com.linkedin.common.urn.DashboardUrn getDashboardUrn(String urnStr) {
    //     try {
    //         return DashboardUrn.createFromString(urnStr);
    //     } catch (URISyntaxException e) {
    //         throw new RuntimeException(String.format("Failed to retrieve dashboard with urn %s, invalid urn", urnStr));
    //     }
    // }

    // @Override
    // public Dashboard update(@Nonnull String urn, @Nonnull DashboardUpdateInput input, @Nonnull QueryContext context) throws Exception {
    //     if (isAuthorized(urn, input, context)) {
    //         final CorpuserUrn actor = CorpuserUrn.createFromString(context.getAuthentication().getActor().toUrnStr());
    //         final Collection<MetadataChangeProposal> proposals = DashboardUpdateInputMapper.map(input, actor);
    //         proposals.forEach(proposal -> proposal.setEntityUrn(UrnUtils.getUrn(urn)));

    //         try {
    //             _entityClient.batchIngestProposals(proposals, context.getAuthentication(), false);
    //         } catch (RemoteInvocationException e) {
    //             throw new RuntimeException(String.format("Failed to write entity with urn %s", urn), e);
    //         }

    //         return load(urn, context).getData();
    //     }
    //     throw new AuthorizationException("Unauthorized to perform this action. Please contact your DataHub administrator.");
    // }

    // private boolean isAuthorized(@Nonnull String urn, @Nonnull DashboardUpdateInput update, @Nonnull QueryContext context) {
    //     // Decide whether the current principal should be allowed to update the Dataset.
    //     final DisjunctivePrivilegeGroup orPrivilegeGroups = getAuthorizedPrivileges(update);
    //     return AuthorizationUtils.isAuthorized(
    //         context.getAuthorizer(),
    //         context.getAuthentication().getActor().toUrnStr(),
    //         PoliciesConfig.DASHBOARD_PRIVILEGES.getResourceType(),
    //         urn,
    //         orPrivilegeGroups);
    // }

    // Useful for future
    // private DisjunctivePrivilegeGroup getAuthorizedPrivileges(final DashboardUpdateInput updateInput) {

    //     final ConjunctivePrivilegeGroup allPrivilegesGroup = new ConjunctivePrivilegeGroup(ImmutableList.of(
    //         PoliciesConfig.EDIT_ENTITY_PRIVILEGE.getType()
    //     ));

    //     List<String> specificPrivileges = new ArrayList<>();
    //     if (updateInput.getOwnership() != null) {
    //         specificPrivileges.add(PoliciesConfig.EDIT_ENTITY_OWNERS_PRIVILEGE.getType());
    //     }
    //     if (updateInput.getEditableProperties() != null) {
    //         specificPrivileges.add(PoliciesConfig.EDIT_ENTITY_DOCS_PRIVILEGE.getType());
    //     }
    //     if (updateInput.getGlobalTags() != null) {
    //         specificPrivileges.add(PoliciesConfig.EDIT_ENTITY_TAGS_PRIVILEGE.getType());
    //     }
    //     final ConjunctivePrivilegeGroup specificPrivilegeGroup = new ConjunctivePrivilegeGroup(specificPrivileges);

    //     // If you either have all entity privileges, or have the specific privileges required, you are authorized.
    //     return new DisjunctivePrivilegeGroup(ImmutableList.of(
    //         allPrivilegesGroup,
    //         specificPrivilegeGroup
    //     ));
    // }
}
