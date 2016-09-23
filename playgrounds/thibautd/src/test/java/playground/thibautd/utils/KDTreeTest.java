/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.thibautd.utils;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.matsim.core.utils.collections.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static playground.meisterk.PersonAnalyseTimesByActivityType.Activities.l;

/**
 * @author thibautd
 */
public class KDTreeTest {
	private static final Logger log = Logger.getLogger( KDTreeTest.class );

	@Test
	public void testClosestEuclidean() {
		final KDTree<int[]> tree = createTree();

		final Random random = new Random( 123 );

		for ( int i=0; i < 50; i++ ) {
			final double[] c = new double[ 3 ];
			for ( int j=0; j < c.length; j++ ) c[ j ] = random.nextDouble() * 100;

			final int[] closest = tree.getClosestEuclidean( c );

			final int[] expected = new int[ 3 ];
			for ( int j=0; j < c.length; j++ ) expected[ j ] = (int) Math.round( c[ j ] );

			Assert.assertArrayEquals(
					"unexpected closest point",
					expected,
					closest );
		}
	}

	@Test
	public void testClosestOneDimension() {
		final KDTree<int[]> tree = createTree();

		final Random random = new Random( 123 );

		for ( int i=0; i < 50; i++ ) {
			final double[] c = new double[ 3 ];
			for ( int j=0; j < c.length; j++ ) c[ j ] = random.nextDouble() * 100;

			final int[] closest = tree.getClosest( c , (c1,c2) -> Math.abs( c1[1] - c2[1] ) );

			final int expected = (int) Math.round( c[1] );

			// only define distance based on one dimension, and check that query is well behaved (there are 100*100 points
			// at the same distance. The query returns one, arbitrary.)
			Assert.assertEquals(
					"unexpected closest point",
					expected,
					closest[1] );
		}
	}

	@Test
	public void testBox() {
		final KDTree<int[]> tree = createTree();

		Collection<int[]> box =
				tree.getBox(
						new double[]{ 20 , 20 , 20 },
						new double[]{ 40 , 40 , 40 } );

		Assert.assertEquals(
				"unexpected number of elements in box",
				21 * 21 * 21,
				box.size() );

		box = tree.getBox(
						new double[]{ 20 , 40 , 20 },
						new double[]{ 40 , 40 , 40 } );

		Assert.assertEquals(
				"unexpected number of elements in box",
				21 * 1 * 21,
				box.size() );

		box = tree.getBox(
						new double[]{ -10 , 20 , 20 },
						new double[]{ 40 , 40 , 40 } );

		Assert.assertEquals(
				"unexpected number of elements in box",
				41 * 21 * 21,
				box.size() );
	}

	@Test
	@Ignore( "made to be ran manually, does not actually test anything")
	public void compareBalance() {
		final long startBalanced = System.currentTimeMillis();
		final KDTree<int[]> balanced = createTree( true );
		final long endBalanced = System.currentTimeMillis();

		log.info( "constructing balanced tree took "+(endBalanced - startBalanced)+"ms" );

		final long startUnbalanced = System.currentTimeMillis();
		final KDTree<int[]> unbalanced = createTree( false );
		final long endUnbalanced = System.currentTimeMillis();

		log.info( "constructing unbalanced tree took "+(endUnbalanced - startUnbalanced)+"ms" );

		long balancedTime = 0;
		long unbalancedTime = 0;

		for ( int i=0; i < 100; i++ ) {
			for ( int j=0; j < 100; j++ ) {
				for ( int k=0; k < 100; k++ ) {
					final double[] c = { i , j , k };

					balancedTime -= System.currentTimeMillis();
					balanced.getClosestEuclidean( c );
					balancedTime += System.currentTimeMillis();

					unbalancedTime -= System.currentTimeMillis();
					unbalanced.getClosestEuclidean( c );
					unbalancedTime += System.currentTimeMillis();
				}
			}
		}

		log.info( "querying balanced tree took "+(balancedTime)+"ms" );
		log.info( "querying unbalanced tree took "+(unbalancedTime)+"ms" );
		log.info( "querying balanced was "+((double) unbalancedTime / balancedTime)+" times faster" );
	}

	private KDTree<int[]> createTree() {
		return createTree( true );
	}

	private KDTree<int[]> createTree( boolean balance ) {
		final KDTree<int[]> tree = new KDTree<>( 3 );

		List<Tuple<int[],double[]>> l = new ArrayList<>();

		for ( int i=0; i < 100; i++ ) {
			for ( int j=0; j < 100; j++ ) {
				for ( int k=0; k < 100; k++ ) {
					final int[] v = { i , j , k };
					final double[] c = { i , j , k };
					l.add( new Tuple<>( v, c ) );
				}
			}
		}

		if ( balance ) {
			tree.add( l );
		}
		else {
			for ( Tuple<int[],double[]> t : l ) tree.add( t.getSecond() , t.getFirst() );
		}

		return tree;
	}
}

