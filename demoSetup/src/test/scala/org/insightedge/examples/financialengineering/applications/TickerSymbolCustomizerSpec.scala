package org.insightedge.examples.financialengineering.applications

import org.insightedge.examples.financialengineering.GigaSpaceSpec
import org.insightedge.examples.financialengineering.SpaceUsage
import org.insightedge.examples.financialengineering.model.TickerSymbol
import org.openspaces.core.GigaSpace
import org.scalatest._
import org.scalatest.FlatSpec

import com.gigaspaces.query.IdQuery

class TickerSymbolCustomizerSpec extends FlatSpec with BeforeAndAfterEach with GigaSpaceSpec with SpaceUsage {

  var datagrid: GigaSpace = _
  
  override def beforeEach() = {
    datagrid = makeClusteredProxy()
    datagrid.clear(null)
  }
 
  "A TickerSymbolCustomizer" should "not load symbols from empty file" in {
    TickerSymbolCustomizer.main(Array("empty-cap-symbol.txt"))

    assert(datagrid.count(null) == 0)
  }

  "A TickerSymbolCustomizer" should "load one symbol" in {
    TickerSymbolCustomizer.main(Array("one-cap-symbol.txt"))

    assert(datagrid.readById(new IdQuery(classOf[TickerSymbol], "TEST")) != null)
  }

  "A TickerSymbolCustomizer" should "load 10 symbols" in {
    TickerSymbolCustomizer.main(Array())

    val symbols = datagrid.readMultiple(new TickerSymbol())
    assert(symbols != null)
    assert(symbols.size == 10) //limit in application.config
  }
  
  "A TickerSymbolCustomizer" should "not load same symbols twice" in {
    TickerSymbolCustomizer.main(Array())
    TickerSymbolCustomizer.main(Array())
    
    val symbols = datagrid.readMultiple(new TickerSymbol())
    assert(symbols != null)
    assert(symbols.size == 10) //limit in application.config
  }
}