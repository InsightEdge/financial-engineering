package org.insightedge.examples.financialengineering

import org.openspaces.core.{GigaSpace, GigaSpaceConfigurer}
import org.openspaces.core.space.UrlSpaceConfigurer

/**
  *
  * User: jason
  *
  * Time: 1:19 PM
  *
  * TODO this trait should be replaced by a connection pool, or possibly just reuse the one that is in the InsightEdge SparkContext.
  */
trait SpaceUsage {

  def makeClusteredProxy(jiniUrl: String = CoreSettings.remoteJiniUrl): GigaSpace = {
    new GigaSpaceConfigurer(new UrlSpaceConfigurer(jiniUrl)).gigaSpace()
  }

}
