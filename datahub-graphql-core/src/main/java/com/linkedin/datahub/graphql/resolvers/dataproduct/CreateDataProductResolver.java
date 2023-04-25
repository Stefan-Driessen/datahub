package com.linkedin.datahub.graphql.resolvers.dataproduct;

import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.CreateDataProductInput;
import com.linkedin.datahub.graphql.generated.OwnerEntityType;
import com.linkedin.datahub.graphql.generated.OwnershipType;
import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.resolvers.mutate.util.OwnerUtils;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.key.DataProductKey;
import com.linkedin.metadata.utils.GenericRecordUtils;
import com.linkedin.mxe.MetadataChangeProposal;
import com.linkedin.dataproduct.DataProductProperties;
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
    // Defined in createDataProduct in \datahub-graphql-core\src\main\resources\entity.graphql
    // final CreateTagInput input = bindArgument(environment.getArgument("input"), CreateTagInput.class);
    final CreateDataProductInput input = bindArgument(environment.getArgument("input"), CreateDataProductInput.class);
    
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Create the Data Product Key
        final DataProductKey key = new DataProductKey();

        // Take user provided name and domain.
        final String dataProductName = input.getName();
        key.setName(dataProductName);
        System.out.println("set Name");
        
        final String domainUrn = input.getDomain();
        key.setDomain(domainUrn);
        System.out.println("set Domain");

        // TODO: Implement existance check.
        // if (_entityClient.exists(EntityKeyUtils.convertEntityKeyToUrn(key, Constants.TAG_ENTITY_NAME), context.getAuthentication())) {
        //   throw new IllegalArgumentException("This Tag already exists!");
        // }

        // Create the MCP 
        final MetadataChangeProposal proposal = new MetadataChangeProposal();
        proposal.setEntityKeyAspect(GenericRecordUtils.serializeAspect(key));
        System.out.println("setEntityKeyAspect");
        proposal.setEntityType(Constants.DATA_PRODUCT_ENTITY_NAME);
        System.out.println("set EntityType");
        proposal.setAspectName(Constants.DATA_PRODUCT_PROPERTIES_ASPECT_NAME);
        System.out.println(String.format("set AspectName as %s", Constants.DATA_PRODUCT_PROPERTIES_ASPECT_NAME));
        System.out.println(String.format("25041518 the serialised aspect is a generic aspect that looks like this: %s", 
                            GenericRecordUtils.serializeAspect(mapDataProductProperties(input))));
        proposal.setAspect(GenericRecordUtils.serializeAspect(mapDataProductProperties(input)));
        System.out.println("set the Aspect");
        proposal.setChangeType(ChangeType.UPSERT);
        System.out.println("set changeType");
        System.out.println(String.format("proposal: %s\ncontext.getAuthentication(): %s", proposal, context.getAuthentication()));
        String dataProductUrn = Urn.createFromTuple(Constants.DATA_PRODUCT_ENTITY_NAME, domainUrn, dataProductName).toString();
        proposal.setEntityUrn(Urn.createFromString(dataProductUrn));
        System.out.println(String.format("set dataProductUrn as %s", dataProductUrn));
        String dpUrn = _entityClient.ingestProposal(proposal, context.getAuthentication());
        System.out.println(String.format("dpUrn: %s =?= dataProductUrn: %s", dpUrn, dataProductUrn));
        OwnerUtils.addCreatorAsOwner(context, dpUrn, OwnerEntityType.CORP_USER, OwnershipType.TECHNICAL_OWNER, _entityService);
        System.out.println("added Creator As Owner");
        System.out.println(String.format("proposal: %s", proposal));
        return dpUrn;
      } catch (Exception e) {
        log.error("Update, 21041333, Failed to create Data Product with name: {}, domain urn: {}: {}", input.getName(), input.getDomain(), e.getMessage());
        throw new RuntimeException(String.format("Failed to create Data Product with name: %s, domain urn: %s", input.getName(), input.getDomain()), e);
      }
    });
  }

  private DataProductProperties mapDataProductProperties(final CreateDataProductInput input) {
    final DataProductProperties result = new DataProductProperties();
    System.out.println("25041217 Gonna propertise this.");
    result.setName(input.getName());
    result.setDescription(input.getDescription());
    System.out.println(String.format("Setting description as: %s", input.getDescription()));
    System.out.println(String.format("result.getName: %s", result.getName()));
    return result;
  }
}