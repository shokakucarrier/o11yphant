package org.commonjava.o11yphant.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import org.commonjava.o11yphant.metrics.api.Gauge;
import org.commonjava.o11yphant.metrics.api.Metric;
import org.commonjava.o11yphant.metrics.impl.O11Meter;
import org.commonjava.o11yphant.metrics.impl.O11Timer;
import org.commonjava.o11yphant.metrics.system.SystemGaugesSet;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static junit.framework.TestCase.assertTrue;

public class DefaultMetricRegistryTest
{
    private static final String THE_TIMER = "timer";

    private static final String THE_METER = "meter";

    private static final String THE_GAUGE = "gauge";

    private DefaultMetricRegistry defaultMetricRegistry;

    @Before
    public void setUp()
    {
        defaultMetricRegistry = new DefaultMetricRegistry( new com.codahale.metrics.MetricRegistry(),
                                                           new HealthCheckRegistry() );
    }

    @Test
    public void testRegister()
    {
        O11Timer o11Timer = new O11Timer();
        defaultMetricRegistry.register( THE_TIMER, o11Timer );

        O11Meter o11Meter = new O11Meter();
        defaultMetricRegistry.register( THE_METER, o11Meter );

        defaultMetricRegistry.register( THE_GAUGE, (Gauge<Long>) () -> 0L );

        Map<String, Metric> standaloneMetrics = defaultMetricRegistry.getMetrics();
        assertTrue( standaloneMetrics.containsKey( THE_TIMER ) );
        assertTrue( standaloneMetrics.containsKey( THE_METER ) );
        assertTrue( standaloneMetrics.containsKey( THE_GAUGE ) );

        // Get underlying codahale metrics, the names should be there as well
        Set<String> names = defaultMetricRegistry.getRegistry().getNames();
        assertTrue( names.contains( THE_TIMER ) );
        assertTrue( names.contains( THE_METER ) );
        assertTrue( names.contains( THE_GAUGE ) );
    }

    @Test
    public void testRegisterMetricSet()
    {
        defaultMetricRegistry.register( "test", new SystemGaugesSet() );

        Map<String, Metric> standaloneMetrics = defaultMetricRegistry.getMetrics();

        assertTrue( standaloneMetrics.containsKey( "test.mem.total.swap" ) );
        assertTrue( standaloneMetrics.containsKey( "test.mem.total.physical" ) );

        standaloneMetrics.entrySet().forEach( entry -> {
            String name = entry.getKey();
            Metric metric = entry.getValue();
            if ( metric instanceof Gauge )
            {
                System.out.println( name + ": " + ( (Gauge) metric ).getValue() );
            }
        } );
    }

}