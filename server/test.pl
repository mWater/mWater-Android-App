#!/usr/bin/perl

use strict;
use warnings;
use Data::Dumper;

open OUT, '>', 'Result.csv' or die "Can't open output file: $!";
# Read and print the first line
chomp ( my $line = <> );
print OUT $line . ",mWater\n";
while ( chomp( $line = <> ) ) {
  next if not $line =~ /^(IDEOS|Nexus),(\d+_.+\.jpg),([^,]*),(.*)$/;
  my ( $dir , $file , $TC, $E_Coli ) = ( $1, $2, $3, $4 );
  next if not -f "$dir/$file";
  print OUT $line.',';
  print "Testing $dir/$file\n";
  my @mWater = `./test $dir/$file`;
  print @mWater;
  my $colony_contours = pop @mWater;
  if ( $colony_contours =~ /^Colony contours: (\d+)$/ ) {
    print OUT $1;
  } else {
    print "mWater FAILED!\n";
  }
  print OUT "\n";
  print "=" x 80 , "\n\n";
}
