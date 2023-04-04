package com.linkedin.datahub.graphql.resolvers.dataproduct;

import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.OwnerEntityType;
import com.linkedin.datahub.graphql.generated.OwnershipType;
import com.linkedin.datahub.graphql.resolvers.mutate.util.OwnerUtils;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.key.dataProductKey;
import com.linkedin.metadata.utils.GenericRecordUtils;
import com.linkedin.mxe.MetadataChangeProposal;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.linkedin.datahub.graphql.resolvers.ResolverUtils.*;

/**
 * Resolver used for creating a new Data Product on DataHub.
 */
@Slf4j
@RequiredArgsConstructor
public class CreateDataProductResolver implements DataFetcher<CompletableFuture<String>> {

  private final EntityClient _entityClient;
  private final EntityService _entityService;

  @Override
  public CompletableFuture<String> get(DataFetchingEnvironment environment) throws Exception {

    final QueryContext context = environment.getContext();
    final String dataProductName = environment.getArgument("name"); // Defined in createDataProduct in \datahub-graphql-core\src\main\resources\entity.graphql
    final String domainUrn = environment.getArgument("domainUrn"); // Defined in createDataProduct in \datahub-graphql-core\src\main\resources\entity.graphql

    return CompletableFuture.supplyAsync(() -> {

      try {
        // Create the Data Product Key
        final dataProductKey key = new dataProductKey();

        // Take user provided id OR generate a random UUID for the Tag.
        key.setName(dataProductName);
        key.setDomain(Urn.createFromString(domainUrn));

        // TODO: Implement existance check.
        // if (_entityClient.exists(EntityKeyUtils.convertEntityKeyToUrn(key, Constants.TAG_ENTITY_NAME), context.getAuthentication())) {
        //   throw new IllegalArgumentException("This Tag already exists!");
        // }

        // Create the MCP 
        final MetadataChangeProposal proposal = new MetadataChangeProposal();
        proposal.setEntityKeyAspect(GenericRecordUtils.serializeAspect(key));
        proposal.setEntityType(Constants.DATA_PRODUCT_ENTITY_NAME); // Already defined in Constants
        // proposal.setAspectName(Constants.TAG_PROPERTIES_ASPECT_NAME); // Presumed not necessary
        // proposal.setAspect(GenericRecordUtils.serializeAspect(mapTagProperties(input)));
        proposal.setChangeType(ChangeType.UPSERT);

        String dataProductUrn = _entityClient.ingestProposal(proposal, context.getAuthentication());
        OwnerUtils.addCreatorAsOwner(context, dataProductUrn, OwnerEntityType.CORP_USER, OwnershipType.TECHNICAL_OWNER, _entityService);
        return dataProductUrn;
      } catch (Exception e) {
        log.error("Failed to create Data Product with name: {}, domain urn: {}: {}", dataProductName, domainUrn, e.getMessage());
        throw new RuntimeException(String.format("Failed to create Data Product with name: %s, domain urn: %s", dataProductName, domainUrn), e);
      }
    });
  }

//   private TagProperties mapTagProperties(final CreateTagInput input) {
//     final TagProperties result = new TagProperties();
//     result.setName(input.getName());
//     result.setDescription(input.getDescription(), SetMode.IGNORE_NULL);
//     return result;
//   }
}