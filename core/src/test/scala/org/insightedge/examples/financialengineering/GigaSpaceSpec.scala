package org.insightedge.examples.financialengineering

import org.openspaces.core.{GigaSpaceConfigurer, GigaSpace}
import org.openspaces.core.cluster.ClusterInfo
import org.openspaces.pu.container.{ProcessingUnitContainerProvider, ProcessingUnitContainer}
import org.scalatest._
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider

trait GigaSpaceSpec extends BeforeAndAfterAll {
  
  this: Suite =>
    
  protected var containerProvider: IntegratedProcessingUnitContainerProvider = _
  protected var container: ProcessingUnitContainer = _

  override def beforeAll() = {
    init()
    super.beforeAll()
  }
  
  override def afterAll() = {
    if (container != null) {
      container.close()
    }
    super.afterAll()
  }

  def init() = {
    containerProvider = new IntegratedProcessingUnitContainerProvider()
    containerProvider.setClusterInfo(createClusterInfo)
    containerProvider.addConfigLocation("classpath:/pu.xml")
    container = containerProvider.createContainer()
  }
 
  def createClusterInfo(): ClusterInfo = {
    val clusterInfo = new ClusterInfo()
    clusterInfo.setSchema("partitioned-sync2backup")
    clusterInfo.setNumberOfInstances(1)
    clusterInfo.setNumberOfBackups(0)
    clusterInfo.setInstanceId(1)
    clusterInfo
  }
}