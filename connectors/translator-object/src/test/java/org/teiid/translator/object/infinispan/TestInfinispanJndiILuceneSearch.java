/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.teiid.translator.object.infinispan;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.teiid.language.Select;
import org.teiid.translator.ExecutionContext;
import org.teiid.translator.object.BasicSearchTest;
import org.teiid.translator.object.ObjectExecution;
import org.teiid.translator.object.infinispan.search.LuceneSearch;
import org.teiid.translator.object.util.TradesCacheSource;
import org.teiid.translator.object.util.VDBUtility;

@SuppressWarnings("nls")
public class TestInfinispanJndiILuceneSearch extends BasicSearchTest {
    private static CacheContainer container = null;
	private static ExecutionContext context;
    
    private InfinispanExecutionFactory factory = null;
		
	@BeforeClass
    public static void beforeEachClass() throws Exception {  
	       // Create the cache manager ...
		container = new DefaultCacheManager("infinispan_persistent_indexing_config.xml"); 
		
		TradesCacheSource.loadCache(container.getCache(TradesCacheSource.TRADES_CACHE_NAME));
		context = mock(ExecutionContext.class);
	}

	@Before public void beforeEachTest() throws Exception{	
        
		factory = new InfinispanExecutionFactory();

		factory.setCacheName(TradesCacheSource.TRADES_CACHE_NAME);
		factory.setRootClassName(TradesCacheSource.TRADE_CLASS_NAME);
		factory.setSearchStrategyClassName(LuceneSearch.class.getName());
		factory.start();
	    

    }

	@Override
	protected List<Object> performTest(Select command, int rowcnt) throws Exception {

		ObjectExecution exec = (ObjectExecution) factory.createExecution(command, context, VDBUtility.RUNTIME_METADATA, container);
		
		exec.execute();
		
		List<Object> rows = new ArrayList<Object>();
		
		int cnt = 0;
		List<Object> row = exec.next();
	
		while (row != null) {
			rows.add(row);
			++cnt;
			row = exec.next();
		}
		
		assertEquals("Did not get expected number of rows", rowcnt, cnt); //$NON-NLS-1$
		
		exec.close();
		return rows;
	}
	
	@Test public void testQueryLikeCriteria1() throws Exception {	
		Select command = (Select)VDBUtility.TRANSLATION_UTILITY.parseCommand("select T.TradeId, T.Name From Trade_Object.Trade as T WHERE T.Name like 'TradeName%'"); //$NON-NLS-1$
					
		List<Object> rows = performTest(command, 3);
		
		compareResultSet(rows);
		
	}	
	
	@Test public void testQueryLikeCriteria2() throws Exception {	
		Select command = (Select)VDBUtility.TRANSLATION_UTILITY.parseCommand("select T.TradeId, T.Name From Trade_Object.Trade as T WHERE T.Name like 'TradeName 2%'"); //$NON-NLS-1$
					
		List<Object> rows = performTest(command, 1);
		
		compareResultSet(rows);
		
	}	
	
	@Test public void testQueryCompareEQBoolean() throws Exception {	
		Select command = (Select)VDBUtility.TRANSLATION_UTILITY.parseCommand("select T.TradeId, T.Name, T.Settled From Trade_Object.Trade as T WHERE T.Settled = 'false'"); //$NON-NLS-1$
					
		List<Object> rows = performTest(command, 2);
		
		compareResultSet(rows);
		
	}	
	
	@Test public void testQueryCompareNEBoolean() throws Exception {	
		Select command = (Select)VDBUtility.TRANSLATION_UTILITY.parseCommand("select T.TradeId, T.Name, T.Settled From Trade_Object.Trade as T WHERE T.Settled <> 'false'"); //$NON-NLS-1$
					
		List<Object> rows = performTest(command, 1);
		
		compareResultSet(rows);
		
	}		
	
	@Test public void testQueryRangeBetween() throws Exception {	
		Select command = (Select)VDBUtility.TRANSLATION_UTILITY.parseCommand("select T.TradeId, T.Name as TradeName From Trade_Object.Trade as T WHERE T.TradeId > '1' and T.TradeId < '3'"); //$NON-NLS-1$
					
		List<Object> rows = performTest(command, 1);
		
		compareResultSet(rows);
		
	}

	@Test public void testQueryRangeAbove() throws Exception {	
		Select command = (Select)VDBUtility.TRANSLATION_UTILITY.parseCommand("select T.TradeId, T.Name as TradeName From Trade_Object.Trade as T WHERE T.TradeId > '1'"); //$NON-NLS-1$
					
		List<Object> rows = performTest(command, 2);
		
		compareResultSet(rows);
		
	}
	
	@Test public void testQueryRangeBelow() throws Exception {	
		Select command = (Select)VDBUtility.TRANSLATION_UTILITY.parseCommand("select T.TradeId, T.Name as TradeName From Trade_Object.Trade as T WHERE T.TradeId < '2'"); //$NON-NLS-1$
					
		List<Object> rows = performTest(command, 1);
		
		compareResultSet(rows);
		
	}	
	
	@Test public void testQueryAnd() throws Exception {	
		Select command = (Select)VDBUtility.TRANSLATION_UTILITY.parseCommand("select T.TradeId, T.Name as TradeName From Trade_Object.Trade as T WHERE T.TradeId > '1' and T.Settled = 'false' "); //$NON-NLS-1$
					
		List<Object> rows = performTest(command, 1);
		
		compareResultSet(rows);
		
	}	
}