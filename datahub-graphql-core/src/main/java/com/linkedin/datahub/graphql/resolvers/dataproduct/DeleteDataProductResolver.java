package com.linkedin.datahub.graphql.resolvers.dataproduct;

import com.datahub.authorization.AuthorizerChain;
import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.entity.client.EntityClient;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;


/**
 * Resolver responsible for hard deleting a particular DataHub data product.
 */
public class DeleteDataProductResolver implements DataFetcher<CompletableFuture<String>> {

  private final EntityClient _entityClient;

  public DeleteDataProductResolver(final EntityClient entityClient) {
    _entityClient = entityClient;
  }

  @Override
  public CompletableFuture<String> get(final DataFetchingEnvironment environment) throws Exception {
    final QueryContext context = environment.getContext();
    final String dataProductUrn = environment.getArgument("urn");
    final Urn urn = Urn.createFromString(dataProductUrn);
    return CompletableFuture.supplyAsync(() -> {
      try {
        _entityClient.deleteEntity(urn, context.getAuthentication());
        if (context.getAuthorizer() instanceof AuthorizerChain) {
          ((AuthorizerChain) context.getAuthorizer()).getDefaultAuthorizer().invalidateCache();
        }
        return dataProductUrn;
      } catch (Exception e) {
        throw new RuntimeException(String.format("Failed to perform delete against data product with urn %s", dataProductUrn), e);
      }
    });
  }
}
