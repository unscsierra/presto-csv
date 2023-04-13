/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ame.presto.csv;

import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.ConnectorSplit;
import com.facebook.presto.spi.ConnectorSplitSource;
import com.facebook.presto.spi.ConnectorTableLayoutHandle;
import com.facebook.presto.spi.FixedSplitSource;
import com.facebook.presto.spi.TableNotFoundException;
import com.facebook.presto.spi.connector.ConnectorSplitManager;
import com.facebook.presto.spi.connector.ConnectorTransactionHandle;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CSVSplitManager
        implements ConnectorSplitManager
{
    private final CSVClient client;

    @Inject
    public CSVSplitManager(CSVClient client)
    {
        this.client = client;
    }

    @Override
    public ConnectorSplitSource getSplits(
            ConnectorTransactionHandle transactionHandle,
            ConnectorSession session,
            ConnectorTableLayoutHandle layout,
            SplitSchedulingContext splitSchedulingContext)
    {
        CSVTableHandle tableHandle = ((CSVTableLayoutHandle) layout).getTableHandle();
        Optional<CSVTable> table = client.getTable(tableHandle.getSchemaName(), tableHandle.getTableName());

        // this can happen if table is removed during a query
        if (!table.isPresent()) {
            throw new TableNotFoundException(tableHandle.getSchemaTableName());
        }

        List<ConnectorSplit> splits = new ArrayList<>();
        splits.add(new CSVSplit(
                tableHandle.getSchemaName(),
                tableHandle.getTableName()));
        Collections.shuffle(splits);
        return new FixedSplitSource(splits);
    }
}
