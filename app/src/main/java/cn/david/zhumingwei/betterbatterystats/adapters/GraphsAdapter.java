/*
 * Copyright (C) 2014 asksven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.david.zhumingwei.betterbatterystats.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.asksven.betterbatterystats.widgets.GraphableBarsPlot;

import java.util.ArrayList;

import cn.david.zhumingwei.betterbatterysatats.R;
import cn.david.zhumingwei.betterbatterystats.data.GraphSerie;
import cn.david.zhumingwei.betterbatterystats.data.GraphSeriesFactory;

public class GraphsAdapter extends ArrayAdapter<String>
{
	private static final String TAG = "GraphsAdapter";
	private final Context m_context;
	private ArrayList<GraphSerie> m_graphs = new ArrayList<GraphSerie>();
	private GraphSeriesFactory m_series = null;
	//protected static ArrayList<HistoryItem> m_histList;

	public GraphsAdapter(Context context, GraphSeriesFactory history)
	{
		super(context, R.layout.credits_row);
		this.m_context = context;
		//m_histList = history;
		m_series = history;
		if (m_series != null)
		{
			this.seriesSetup();
		}
	}

//	public void setList(ArrayList<HistoryItem> history)
//	{
//		m_histList = history;
//		m_graphs.clear();
//		this.seriesSetup();
//	}
	
	public void setSeries(GraphSeriesFactory series)
	{
		m_series = series;
		m_graphs.clear();
		this.seriesSetup();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) m_context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
        	convertView = inflater.inflate(R.layout.timeline, null);
        }

		TextView textViewTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
		textViewTitle.setText(m_graphs.get(position).getTitle());
		
        GraphableBarsPlot bars = (GraphableBarsPlot) convertView.findViewById(R.id.Timeline);
        bars.setValues(m_graphs.get(position).getValues());


		return convertView;
	}
	
	public int getCount()
    {
    	if (m_graphs != null)
    	{
    		return m_graphs.size();
    	}
    	else
    	{
    		return 0;
    	}
    }

    public String getItem(int position)
    {
        return m_graphs.get(position).getTitle();
    }

    public long getItemId(int position)
    {
        return position;
    }
    
	private void seriesSetup()
    {
        // SERIES #2:
        GraphSerie mySerie1 = new GraphSerie(
        		m_context.getString(R.string.label_graph_wakelock),
        		m_series.getValues(GraphSeriesFactory.SERIE_WAKELOCK));

        m_graphs.add(mySerie1);	        

        
        // SERIES #3:
        GraphSerie mySerie2 = new GraphSerie(
        		m_context.getString(R.string.label_graph_screen),
        		m_series.getValues(GraphSeriesFactory.SERIE_SCREENON));

		m_graphs.add(mySerie2);	        


        // SERIES #4:
		GraphSerie mySerie3 = new GraphSerie(
        		m_context.getString(R.string.label_graph_wifi),
        		m_series.getValues(GraphSeriesFactory.SERIE_WIFI));

		m_graphs.add(mySerie3);	        


        // SERIES #4:
		GraphSerie mySerie4 = new GraphSerie(
        		m_context.getString(R.string.label_graph_power),
        		m_series.getValues(GraphSeriesFactory.SERIE_CHARGING));

		m_graphs.add(mySerie4);	                

        // SERIES #6:
		GraphSerie mySerie6 = new GraphSerie(
        		m_context.getString(R.string.label_graph_gps),
        		m_series.getValues(GraphSeriesFactory.SERIE_GPS));

		m_graphs.add(mySerie6);	        


        // SERIES #7:
		GraphSerie mySerie7 = new GraphSerie(
        		m_context.getString(R.string.label_graph_bluetooth),
        		m_series.getValues(GraphSeriesFactory.SERIE_BT));

		m_graphs.add(mySerie7);	        



    }

	
}