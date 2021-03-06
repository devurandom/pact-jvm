package au.com.dius.pact.consumer

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.model.RequestResponsePact
import au.com.dius.pact.model.matchingrules.MatchingRuleGroup
import au.com.dius.pact.model.matchingrules.RegexMatcher
import org.apache.http.HttpStatus
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.junit.Rule
import org.junit.Test

@SuppressWarnings(['PublicInstanceField', 'NonFinalPublicField', 'JUnitPublicNonTestMethod',
  'JUnitTestMethodWithoutAssert'])
class V2MatchingHeaderTest {

  @Rule
  public PactProviderRuleMk2 provider = new PactProviderRuleMk2('786_provider', PactSpecVersion.V2, this)

  @Pact(provider = '786_provider', consumer = 'test_consumer')
  RequestResponsePact createPact(PactDslWithProvider builder) {
    Map<String, String> headers = ['Header-A': 'A', 'Header-B': 'B']
    RequestResponsePact pact = builder
      .uponReceiving('a request with headers')
      .method('GET')
      .path('/')
      .willRespondWith()
      .status(HttpStatus.SC_OK)
      .body('{}', ContentType.APPLICATION_JSON)
      .headers(headers)
      .matchHeader('Content-Type', 'application/json;\\s?charset=(utf|UTF)-8')
      .toPact()

    assert pact.interactions.first().response.matchingRules.rulesForCategory('header').matchingRules == [
      'Content-Type': new MatchingRuleGroup([new RegexMatcher('application/json;\\s?charset=(utf|UTF)-8')])
    ]
    assert pact.interactions.first().response.matchingRules.rulesForCategory('header')
      .toMap(PactSpecVersion.V2) == [
      '$.header.Content-Type': [match: 'regex', regex: 'application/json;\\s?charset=(utf|UTF)-8']
    ]

    pact
  }

  @Test
  @PactVerification('786_provider')
  void runTest() {
    Request.Get(provider.url).execute().returnContent().asString()
  }

}
